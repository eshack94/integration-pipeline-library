#!/usr/bin/groovy

def call(String stageName = 'Setup', Closure body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    def tool = config.tool
    def exe = config.exe

    ProjectUtils projectUtils = new ProjectUtils()
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
    def process = "whoami".execute()
    process.waitFor()
    println process.getText()

    def mavenProcess = "${mavenExe} help:evaluate -Dexpression=project.version | grep -v '\\['".execute()
    mavenProcess.waitFor()
    return mavenProcess.getText()
}

public String getMavenProjectVersionParse(){
    def fileText =  new File('pom.xml').text
    def project = new XmlSlurper().parseText(fileText)
    return project.version.text()
}


public String getGradleProjectVersionProcess(String exe){
   def gradleExe = './gradlew'
   if(exe) {
        gradleExe = exe
   }
   def gradleProcess = "${gradleExe} properties -q | grep 'version:'".execute().waitFor()
   gradleVersion = gradleProcess.getText()
   return gradleVersion.substring(gradleVersion.indexOf(':') + 1).trim()
}

public String getGradleProjectVersionParse(){
   def versionLine = ''
   new File('build.gradle').eachLine { line ->
       def trimmedLine = line.trim()
       if (!versionLine && trimmedLine.startsWith('version')) {
           versionLine = trimmedLine;
       }
   }
   return versionLine.substring(versionLine.indexOf('=') + 1).trim()
}
