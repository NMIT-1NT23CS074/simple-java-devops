pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven3'
    }

    environment {
        DOCKER_CREDENTIALS = 'dockerhub'                      // Jenkins Docker credentials ID
        DOCKER_IMAGE = "devaraj74/simple-java-app:${BUILD_NUMBER}"    // Full image name with tag
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
        timeout(time: 10, unit: 'MINUTES') {
            waitForQualityGate abortPipeline: true
        }
    }
}

   stage('6.OWASP Dependency Check') {
    environment {
        NVD_API_KEY = credentials('NVD_API_KEY') // Jenkins secret
    }
    steps {
        script {
            // Run Maven Dependency Check with NVD API Key
            sh """
            mvn org.owasp:dependency-check-maven:check \
                -Dnvd.api.key=${NVD_API_KEY} \
                -Dformat=HTML \
                -DfailOnError=true
            """
        }
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
                sh 'docker images'
                sh 'echo $DOCKER_IMAGE'
            }
        }

        stage('8. Push & Deploy') {
            steps {
                script {
                    // Push Docker image to Docker Hub
                    docker.withRegistry('https://index.docker.io/v1/', "${DOCKER_CREDENTIALS}") {
                        sh 'docker push $DOCKER_IMAGE'
                    }
                }

                sshagent(['build-server-ssh']) {
                    // Deploy on remote server via SSH
                    sh """
                    ssh -o StrictHostKeyChecking=no ubuntu@18.234.101.52 \\
                    "docker pull $DOCKER_IMAGE && \\
                     docker stop simple-java-app || true && \\
                     docker rm simple-java-app || true && \\
                     docker run -d --name simple-java-app -p 80:8080 $DOCKER_IMAGE
                    """
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
