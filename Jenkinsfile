pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    environment {
        DOCKER_HOST = 'unix:///var/run/docker.sock'
        TESTCONTAINERS_RYUK_DISABLED = 'true'
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'Checking out source...'
                checkout scm
            }
        }

        stage('Build Common') {
            steps {
                echo 'Building shared library...'
                sh 'mvn install -pl common -am -DskipTests -q'
            }
        }

        stage('Unit Tests') {
            steps {
                echo 'Running unit tests (no Docker needed)...'
                sh '''
                    mvn test \
                        -pl ingestion-service,flight-tracker-service \
                        -am \
                        -Dexcludes="**/*IntegrationTest.java"
                '''
                sh 'find . -name "*.xml" -path "*/surefire-reports/*" || echo "No surefire reports found"'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Integration Tests') {
            steps {
                echo 'Running integration tests (Testcontainers — needs Docker)...'
                sh '''
                    mvn test \
                        -pl ingestion-service,flight-tracker-service \
                        -am \
                        -Dincludes="**/*IntegrationTest.java"
                '''
                sh 'find . -name "*.xml" -path "*/surefire-reports/*" || echo "No surefire reports found"'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging JARs...'
                sh 'mvn package -DskipTests -q'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
                }
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline passed'
        }
        failure {
            echo '❌ Pipeline failed — check the logs above'
        }
        always {
            cleanWs()
        }
    }
}