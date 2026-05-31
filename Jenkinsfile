pipeline {
    agent any

    environment {
        JAVA_HOME = tool 'JDK21'
        PATH      = "${JAVA_HOME}/bin:${env.PATH}"
        MVN       = './production-system/mvnw'
        WORKDIR   = 'production-system'
    }

    stages {

        // ─────────────────────────────────────────────────
        // ANA — Build e verificação do projeto
        // ─────────────────────────────────────────────────
        stage('Build - Ana') {
            steps {
                dir("${WORKDIR}") {
                    sh 'chmod +x mvnw'
                    sh './mvnw clean compile -q'
                }
            }
            post {
                success { echo 'Build concluído com sucesso.' }
                failure { echo 'Falha no build. Verifique erros de compilação.' }
            }
        }

    }

    post {
        always {
            echo "Pipeline finalizada — branch: ${env.BRANCH_NAME ?: 'local'}"
        }
        success {
            echo 'Todos os stages concluídos com sucesso.'
        }
        failure {
            echo 'Pipeline falhou. Verifique os logs acima.'
        }
    }
}