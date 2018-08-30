#!/usr/bin/groovy

def call(Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    String emailListVar = config.emailList
    String gitUrlVar = config.gitUrl
    String gitRelativeTargetDirVar = config.gitRelativeTargetDir

    Closure preBuildBody = config.preBuild
    String gradleCommandVar = config.buildCommand
    Closure postBuildBody = config.postBuild

    String detectCommandVar = config.detectCommand

    boolean runGitHubReleaseVar = config.get('runGitHubRelease', true)
    String gradleExeVar = config.gradleExe
    String releaseVersionVar = config.releaseVersion
    String ownerVar = config.owner
    String artifactFileVar = config.artifactFile
    String artifactPatternVar = config.artifactPattern
    String artifactDirectoryVar = config.artifactDirectory
    String projectVar = config.project
    String releaseDescriptionVar = config.releaseDescription

    boolean runArchiveVar = config.get('runArchive', true)
    String archivePatternVar = config.archivePattern

    boolean runJunitVar = config.get('runJunit', true)
    String junitXmlPatternVar = config.junitXmlPattern

    boolean runJacocoVar = config.get('runJacoco', true)

    boolean runReleaseVar
    try {
        runReleaseVar = config.get('runRelease', Boolean.valueOf("${RUN_RELEASE}"))
    } catch (MissingPropertyException e) {
        runReleaseVar = false
    }
    boolean checkAllDependenciesVar = config.get('checkAllDependencies', false)
    println "Going to run the Release ${runReleaseVar}"

    integrationNode {
        emailWrapper(emailListVar) {
            setupStage {
                setJdk {}
            }
            def directoryToRunIn = testGitStage {
                url = gitUrlVar
                gitRelativeTargetDir = gitRelativeTargetDirVar
            }
            dir(directoryToRunIn) {
                if (runReleaseVar) {
                    preReleaseStage {
                        buildTool = 'gradle'
                        exe = gradleExeVar
                        checkAllDependencies = checkAllDependenciesVar
                    }
                }
                if (null != preBuildBody) {
                    stage('Pre Build') {
                        preBuildBody()
                    }
                }
                gradleStage {
                    buildCommand = gradleCommandVar
                }
                if (null != postBuildBody) {
                    stage('Post Build') {
                        postBuildBody()
                    }
                }
                detectStage {
                    detectCommand = detectCommandVar
                }
                if (runGitHubReleaseVar) {
                    newGarStage {
                        buildTool = 'gradle'
                        exe = gradleExeVar
                        releaseVersion = releaseVersionVar
                        owner = ownerVar
                        artifactFile = artifactFileVar
                        artifactPattern = artifactPatternVar
                        artifactDirectory = artifactDirectoryVar
                        project = projectVar
                        releaseDescription = releaseDescriptionVar
                    }
                }
                if (runReleaseVar) {
                    postReleaseStage {
                        buildTool = 'gradle'
                        exe = gradleExeVar
                    }
                }
                if (runArchiveVar) {
                    archiveStage {
                        patterns = archivePatternVar
                    }
                }
                if (runJunitVar) {
                    junitStage {
                        xmlPattern = junitXmlPatternVar
                    }
                }
                if (runJacocoVar) {
                    jacocoStage {}
                }
            }
        }
    }
}