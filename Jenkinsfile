
pipeline {
    agent any
    environment {
        DOCKER_IMAGE_NAME = "study-pal:latest"
        DOCKER_IMAGE_FILE = "study-pal.tar.gz"
    }

    stages {
        stage('Notify Start') {
            when {
                beforeAgent true
            }
            steps {
                script {
                    def sendDiscordMessage = { content ->
                        sh """
                            curl -H "Content-Type: application/json" \
                                 -X POST \
                                 -d '{ "content": "${content.replaceAll('"', '\\"')}" }' \
                                 ${DISCORD_WEBHOOK}
                        """
                    }
                    sendDiscordMessage("🚀 Jenkins 파이프라인이 시작되었습니다.")
                }
            }
        }

        stage('CheckOut') {
            steps {
                checkout scm
            }
        }

        stage('Test') {
            steps {
                withEnv([
                    "MYSQL_HOST=${MYSQL_HOST}",
                    "MYSQL_PORT=${MYSQL_PORT}",
                    "MYSQL_USER=${MYSQL_USER}",
                    "MYSQL_PWD=${MYSQL_PWD}"
                ]) {
                    sh './gradlew test'
                }
            }
        }

        stage('Build') {
            steps {
                sh './gradlew clean build'
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
                        -e MYSQL_HOST=${MYSQL_HOST} \\
                        -e MYSQL_PORT=${MYSQL_PORT} \\
                        -e MYSQL_USER=${MYSQL_USER} \\
                        -e MYSQL_PWD=${MYSQL_PWD} \\
                            ${DOCKER_IMAGE_NAME}
                    """

                    sh "docker system prune -f"
                }
            }
        }
    }
    post {
        success {
            echo 'Build and archive completed successfully!'
            script {
                def sendDiscordMessage = { content ->
                    sh """
                        curl -H "Content-Type: application/json" \
                             -X POST \
                             -d '{ "content": "${content.replaceAll('"', '\\"')}" }' \
                             ${DISCORD_WEBHOOK}
                    """
                }
                sendDiscordMessage("✅ 파이프라인이 성공적으로 완료되었습니다!")
            }
        }
        failure {
            echo 'Build or archive failed'
            script {
                def sendDiscordMessage = { content ->
                    sh """
                        curl -H "Content-Type: application/json" \
                             -X POST \
                             -d '{ "content": "${content.replaceAll('"', '\\"')}" }' \
                             ${DISCORD_WEBHOOK}
                    """
                }
                sendDiscordMessage("❌ 파이프라인이 실패했습니다. 자세한 로그를 확인해주세요.")
            }
        }
    }
}