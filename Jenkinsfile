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

        // ─────────────────────────────────────────────────
        // PETTRIUS — Testes de Controller e Repository
        // ─────────────────────────────────────────────────
        stage('Testes Controller e Repository - Pettrius') {
            steps {
                dir("${WORKDIR}") {
                    sh 'chmod +x mvnw'
                    sh './mvnw test -Dspring.profiles.active=test -Dtest="*ControllerTest,*RepositoryTest"'
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'production-system/target/surefire-reports/*.xml'
                }
                success { echo 'Testes de Controller e Repository passaram.' }
                failure { echo 'Falha nos testes de Controller/Repository.' }
            }
        }

        // ─────────────────────────────────────────────────
        // Vinicius — Testes Unitários (Service e Entity)
        // ─────────────────────────────────────────────────
        stage('Testes Service e Entity - Vinicius') {
            steps {
                dir("${WORKDIR}") {
                    sh 'chmod +x mvnw'
                    sh '''
                        ./mvnw test \
                        -Dspring.profiles.active=test \
                        -Dtest="*ServiceTest,*EntityTest"
                    '''
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'production-system/target/surefire-reports/*.xml'
                }
                success {
                    echo 'Testes de Service e Entity executados com sucesso.'
                }
                failure {
                    echo 'Falha nos testes de Service e Entity.'
                }
            }
        }

        // ─────────────────────────────────────────────────
        // João — Gera o arquivo .jar e arquiva no jenkins
        // ─────────────────────────────────────────────────
        stage('Package - João') {
            steps {
                dir("${WORKDIR}") {
                    sh 'chmod +x mvnw'
                    sh './mvnw package -DskipTests'
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        // ─────────────────────────────────────────────────
        // ANA — Docker Build da imagem da aplicação
        // ─────────────────────────────────────────────────
        stage('Docker Build - Ana') {
            steps {
                dir("${WORKDIR}") {
                    sh 'docker build -t production-system:${BUILD_NUMBER} .'
                }
            }
            post {
                success { echo 'Imagem Docker gerada com sucesso.' }
                failure { echo 'Falha ao gerar imagem Docker.' }
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