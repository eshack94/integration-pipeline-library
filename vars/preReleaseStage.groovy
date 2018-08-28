#!/usr/bin/groovy

import com.synopsys.integration.ProjectUtils

def call(String stageName = 'Pre-Release Stage', Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def runRelease = config.runRelease
    def buildTool = config.buildTool
    def exe = config.exe
    def checkAllDependencies = config.get('checkAllDependencies', false)

    def branch = config.get('branch', "${BRANCH}")

    stage(stageName) {
        ProjectUtils projectUtils = new ProjectUtils(this)
        if (runRelease) {
            def hasMavenSnapshotDependencies = projectUtils.checkForSnapshotDependencies('maven', exe, checkAllDependencies)
            def hasGradleSnapshotDependencies = projectUtils.checkForSnapshotDependencies('gradle', exe, checkAllDependencies)
            if (hasMavenSnapshotDependencies || hasGradleSnapshotDependencies) {
                def errorMessage = ''
                if (hasMavenSnapshotDependencies) {
                    errorMessage += 'Failing release build because of Maven SNAPSHOT dependencies'
                }
                if (hasGradleSnapshotDependencies) {
                    errorMessage += 'Failing release build because of Gradle SNAPSHOT dependencies'
                }
                throw new Exception(errorMessage)
            }
            def version = projectUtils.getProjectVersion('maven', exe)

            if (version.contains('-SNAPSHOT')) {
                println "Removing SNAPSHOT from the Project Version"
                def newMavenVersion = projectUtils.removeSnapshotFromProjectVersion('maven', exe)
                def newGradleVersion = projectUtils.removeSnapshotFromProjectVersion('gradle', exe)
                println "Commiting the release ${newMavenVersion}  ${newGradleVersion}"
                sh "git commit -am \"Release ${newMavenVersion}  ${newGradleVersion}\""
                println "Pushing release to branhc ${branch}"
                sh "git push origin ${branch}"
            }
        } else {
            println "Skipping the Pre-Release because this was not a release build"
        }
    }
}
