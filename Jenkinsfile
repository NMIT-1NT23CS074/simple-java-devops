pipeline {
    agent any

    environment {
        DOCKER_CREDENTIALS = 'dockerhub'
        SSH_CREDENTIALS = 'build-server-ssh'
        DOCKER_IMAGE = 'devaraj74/simple-java-app'
        SONARQUBE_SERVER = 'mysonar'
    }

    stages {

        stage('Checkout') {
            steps {
              git branch: 'main', 
    credentialsId: 'git', 
    url: 'https://github.com/NMIT-1NT23CS074/simple-java-devops.git'
                
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_SERVER}") {
                    sh 'sonar-scanner -Dsonar.projectKey=simple-java-app -Dsonar.sources=src/main/java'
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', "${DOCKER_CREDENTIALS}") {
                        def appImage = docker.build("${DOCKER_IMAGE}:latest")
                        appImage.push()
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                sshagent(['build-server-ssh']) {
                    sh '''
                    ssh -o StrictHostKeyChecking=no ubuntu@44.201.194.98 "
                    docker pull yourdockerhubusername/simple-java-app:latest &&
                    docker stop simple-java-app || true &&
                    docker rm simple-java-app || true &&
                    docker run -d --name simple-java-app -p 8080:8080 devaraj74/simple-java-app:latest
                    "
                    '''
                }
            }
        }

    }

    post {
        always {
            cleanWs()
        }
    }
}
