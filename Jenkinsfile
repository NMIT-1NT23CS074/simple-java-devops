pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven3'
    }

    environment {
        DOCKER_CREDENTIALS = 'dockerhub'
        DOCKER_IMAGE = 'devaraj74/simple-java-app:latest'
        SONARQUBE_SERVER = 'mysonar'
    }

    stages {

        stage('1. Checkout') {
            steps {
                git branch: 'main',
                credentialsId: 'git',
                url: 'https://github.com/NMIT-1NT23CS074/simple-java-devops.git'
            }
        }

        stage('2. Build') {
            steps {
                sh 'mvn clean package'
            }
        }

        stage('3. Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('4. SonarQube Analysis') {
            steps {
                withSonarQubeEnv("${SONARQUBE_SERVER}") {
                    sh 'mvn clean verify sonar:sonar'
                }
            }
        }

        stage('5. Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

       stage('OWASP Dependency Check') {
    steps {
        dependencyCheck additionalArguments: '--scan .', odcInstallation: 'odc'
        dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
    }
}

        stage('7. Docker Build & Trivy Scan') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE .'
                sh 'trivy image --exit-code 0 --severity HIGH,CRITICAL $DOCKER_IMAGE'
            }
        }

        stage('7.1 Debug Docker Image') {
    steps {
        sh 'docker images'          // List local images to verify $DOCKER_IMAGE exists
        sh 'echo $DOCKER_IMAGE'     // Print the variable to confirm correct value
    }
}
        

     stage('8. Push & Deploy') {
    steps {
        script {
            // Push to Docker Hub
            docker.withRegistry('https://index.docker.io/v1/', "${DOCKER_CREDENTIALS}") {
                sh 'docker push $DOCKER_IMAGE'
            }
        }

        sshagent(['build-server-ssh']) {
            sh """
            ssh -o StrictHostKeyChecking=no ubuntu@54.204.116.34 \\
            "docker pull $DOCKER_IMAGE && \\
             docker stop simple-java-app || true && \\
             docker rm simple-java-app || true && \\
             docker run -d --name simple-java-app $DOCKER_IMAGE"
            """
        }
    }
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
