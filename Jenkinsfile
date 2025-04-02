pipeline {
    agent any
    environment {
        DOCKER_IMAGE_NAME = "study-pal:latest"
        DOCKER_IMAGE_FILE = "study-pal.tar.gz"
    }

    stages {
        stage('CheckOut') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                withEnv([

                ]) {
                    sh './gradlew test'
                }
            }
        }

        stage('Build') {
            steps {
                sh './gradlew build'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh 'docker build -t ${DOCKER_IMAGE_NAME} .'
                }
            }
        }

        stage('Save Docker Image') {
            steps {
                sh "docker save ${DOCKER_IMAGE_NAME} | gzip > ${DOCKER_IMAGE_FILE}"
                archiveArtifacts artifacts: "${DOCKER_IMAGE_FILE}", allowEmptyArchive: false
            }
        }

        stage('Deploy Docker Image Locally') {
            steps {
                script {
                    // Load the Docker image from file
                    sh "docker load -i ${DOCKER_IMAGE_FILE}"

                    sh """
                        docker stop study-pal-container || true
                        docker rm study-pal-container || true
                    """

                    sh """
                        docker run -d --name study-pal-container -p 8080:8080 \\
                            ${DOCKER_IMAGE}
                    """

                    sh "docker system prune -f"
                }
            }
        }
    }
    post {
        success {
            echo 'Build and archive completed successfully!'
        }
        failure {
            echo 'Build or archive failed'
        }
    }
}