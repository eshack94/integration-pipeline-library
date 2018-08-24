#!/usr/bin/groovy

def call(String stageName = 'Setup', Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def tool = config.tool
    def exe = config.exe

    if(tool.equals('maven')){
        sh "echo ${getMavenProjectVersionProcess(exe)}"
        sh "echo ${getMavenProjectVersionParse()}"
    } else {
        sh "echo ${getGradleProjectVersionProcess(exe)}"
        sh "echo ${getGradleProjectVersionParse()}"
    }
}


public String getMavenProjectVersionProcess(String exe){
    def mavenExe = 'mvn'
    if(exe) {
        mavenExe = exe
    }
    def version = sh(script: "${mavenExe} help:evaluate -Dexpression=project.version | grep -v '\\['", returnStdout: true)
    return version
}

public String getMavenProjectVersionParse(){
    def files = findFiles glob: 'pom.xml'
    def fileText = files[0].text
    def project = new XmlSlurper().parseText(fileText)
    return project.version.text()
}


public String getGradleProjectVersionProcess(String exe){
   def gradleExe = './gradlew'
   if(exe) {
        gradleExe = exe
   }
   def version = sh(script: "${gradleExe} properties -q | grep 'version:'", returnStdout: true)
   return version.substring(version.indexOf(':') + 1).trim()
}

public String getGradleProjectVersionParse(){
   def versionLine = ''
   def files = findFiles glob: 'build.gradle'
   files[0].eachLine { line ->
       def trimmedLine = line.trim()
       if (!versionLine && trimmedLine.startsWith('version')) {
           versionLine = trimmedLine;
       }
   }
   return versionLine.substring(versionLine.indexOf('=') + 1).trim()
}
