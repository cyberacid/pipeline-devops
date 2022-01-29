/*
	forma de invocación de método call:
	def ejecucion = load 'script.groovy'
	ejecucion.call()
*/
def call(stages){

    def stagesList = stages.split(";")
    sh "echo ${stagesList}"

    sBuild()
    sSonar()
    sCurl()
    sNexusUpload()
    sNexusDownload()
    sJar()
    sTest()
}
def sBuild() {
    stage("Paso 1: Build && Test"){
        env.TAREA = env.STAGE_NAME
        sh "gradle clean build"
    }

}
    
def sSonar() {
    stage("Paso 2: Sonar - Análisis Estático"){
        env.TAREA = env.STAGE_NAME
        sh "echo 'Análisis Estático!'"
        withSonarQubeEnv('sonarqube') {
            sh './gradlew sonarqube -Dsonar.projectKey=ejemplo-gradle -Dsonar.java.binaries=build'
        }
    }

}

def sCurl() {
    stage("Paso 3: Curl Springboot Gradle sleep 20"){
        env.TAREA = env.STAGE_NAME
        sh "gradle bootRun&"
        sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }

}
    
def sNexusUpload() {
    stage("Paso 4: Subir Nexus"){
        env.TAREA = env.STAGE_NAME
        nexusPublisher nexusInstanceId: 'nexus',
        nexusRepositoryId: 'devops-usach-nexus',
        packages: [
            [$class: 'MavenPackage',
                mavenAssetList: [
                    [classifier: '',
                    extension: 'jar',
                    filePath: 'build/libs/DevOpsUsach2020-0.0.1.jar'
                ]
            ],
                mavenCoordinate: [
                    artifactId: 'DevOpsUsach2020',
                    groupId: 'com.devopsusach2020',
                    packaging: 'jar',
                    version: '0.0.1'
                ]
            ]
        ]
    }

} 

def sNexusDownload() {
    stage("Paso 5: Descargar Nexus"){
        env.TAREA = env.STAGE_NAME
        sh ' curl -X GET -u $NEXUS_USER:$NEXUS_PASSWORD "http://nexus:8081/repository/devops-usach-nexus/com/devopsusach2020/DevOpsUsach2020/0.0.1/DevOpsUsach2020-0.0.1.jar" -O'
    }
}
    
def sJar() {
    stage("Paso 6: Levantar Artefacto Jar"){
        env.TAREA = env.STAGE_NAME
        sh 'nohup bash java -jar DevOpsUsach2020-0.0.1.jar & >/dev/null'
    }
}
    
def sTest() {
    stage("Paso 7: Testear Artefacto - Dormir(Esperar 20sg) "){
        env.TAREA = env.STAGE_NAME
        sh "sleep 20 && curl -X GET 'http://localhost:8081/rest/mscovid/test?msg=testing'"
    }
}
return this;