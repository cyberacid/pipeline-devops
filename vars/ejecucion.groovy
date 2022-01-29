pipeline {
    agent any
    environment {
        NEXUS_USER         = credentials('user-nexus')
        NEXUS_PASSWORD     = credentials('password-nexus')
    }
    parameters {
        choice(
            name:'compileTool',
            choices: ['Maven', 'Gradle'],
            description: 'Seleccione herramienta de compilacion'
        )
    }
    stages {
        stage("Pipeline"){
            steps {
                script{
                  switch(params.compileTool)
                    {
                        case 'Maven':
                            def ejecucion = load 'maven.groovy'
                            ejecucion.call()
                        break;
                        case 'Gradle':
                            def ejecucion = load 'gradle.groovy'
                            ejecucion.call()
                        break;
                    }
                }
            }
            post{
                success{
                    slackSend channel: 'U02MXGDEZT5', color: 'good', message: "[Fernando Peña] [${JOB_NAME}] [${BUILD_TAG}] Ejecucion Exitosa", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'slack'
                }
                failure{
                    slackSend channel: 'U02MXGDEZT5', color: 'danger', message: "[Fernando Peña] [${env.JOB_NAME}] [${BUILD_TAG}] Ejecucion fallida en stage [${TAREA}]", teamDomain: 'dipdevopsusac-tr94431', tokenCredentialId: 'slack'
                }
            }
        }
    }
}