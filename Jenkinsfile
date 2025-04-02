

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
            script {
                def branch = sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
                def commitMsg = sh(script: "git log -1 --pretty=%s", returnStdout: true).trim()
                def author = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
                def shortSha = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()

                def jenkinsUrl = env.BUILD_URL.replace("192.168.219.135:8180", "jack8226.ddns.net:3005")
                def reportUrl = "${jenkinsUrl}ws/build/reports/tests/test/index.html"

                def desc = """
    ━━━━━━━━━━━━━━━━━━━━━━━━━━
    📦 study-pal Jenkins Pipeline

    🔧 결과: ✅ 빌드 성공
    🌿 브랜치: ${branch}
    ✍️ 커밋 메시지:
    ${commitMsg}
    🧑‍💻 작성자: ${author}
    🔗 SHA: ${shortSha}

    📄 테스트 리포트 보기: ${reportUrl}
    ━━━━━━━━━━━━━━━━━━━━━━━━━━
    """.stripIndent().trim()

                discordSend(
                    description: desc,
                    link: jenkinsUrl,
                    result: currentBuild.currentResult,
                    title: "📦 study-pal Jenkins Pipeline",
                    footer: "jack8226.ddns.net:3005",
                    webhookURL: "${DISCORD_WEBHOOK}"
                )
            }
        }

        failure {
            script {
                def branch = sh(script: "git rev-parse --abbrev-ref HEAD", returnStdout: true).trim()
                def commitMsg = sh(script: "git log -1 --pretty=%s", returnStdout: true).trim()
                def author = sh(script: "git log -1 --pretty=%an", returnStdout: true).trim()
                def shortSha = sh(script: "git rev-parse --short HEAD", returnStdout: true).trim()

                def jenkinsUrl = env.BUILD_URL.replace("192.168.219.135:8180", "jack8226.ddns.net:3005")
                def reportUrl = "${jenkinsUrl}ws/build/reports/tests/test/index.html"

                def desc = """
    ━━━━━━━━━━━━━━━━━━━━━━━━━━
    📦 study-pal Jenkins Pipeline

    🔧 결과: ❌ 빌드 실패
    🌿 브랜치: ${branch}
    ✍️ 커밋 메시지:
    ${commitMsg}
    🧑‍💻 작성자: ${author}
    🔗 SHA: ${shortSha}

    📄 테스트 리포트 보기: ${reportUrl}
    ━━━━━━━━━━━━━━━━━━━━━━━━━━
    """.stripIndent().trim()

                discordSend(
                    description: desc,
                    link: jenkinsUrl,
                    result: currentBuild.currentResult,
                    title: "📦 study-pal Jenkins Pipeline",
                    footer: "jack8226.ddns.net:3005",
                    webhookURL: "${DISCORD_WEBHOOK}"
                )
            }
        }
    }
}