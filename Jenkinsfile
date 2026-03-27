pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven3'
    }

    environment {
        DOCKER_CREDENTIALS = 'dockerhub'                      
        DOCKER_IMAGE = "devaraj74/simple-java-app:${BUILD_NUMBER}"    
        SONARQUBE_SERVER = 'mysonar'                          
        NVD_API_KEY = credentials('NVD_API_KEY')              
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
                    sh 'mvn clean verify sonar:sonar -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml'
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

        stage('6. OWASP Dependency Check (Docker)') {
            steps {
                script {
                    // Use sudo if Docker permission denied
                    sh '''
                        sudo docker run --rm -v $PWD:/src -e NVD_API_KEY=$NVD_API_KEY owasp/dependency-check:12.3.0 \
                        --project "simple-java-app" \
                        --scan /src/target \
                        --format "HTML" \
                        --format "JSON" \
                        --format "XML" \
                        --out /src/dependency-check-report
                    '''
                }
            }
        }

        stage('7. Docker Build & Trivy Scan') {
            steps {
                script {
                    // Build Docker image
                    sh 'sudo docker build -t $DOCKER_IMAGE .'
                    // Scan Docker image with Trivy
                    sh 'sudo trivy image --exit-code 0 --severity HIGH,CRITICAL $DOCKER_IMAGE'
                }
            }
        }

        stage('7.1 Debug Docker Image') {
            steps {
                sh 'sudo docker images'
                sh 'echo $DOCKER_IMAGE'
            }
        }

        stage('8. Push & Deploy') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', "${DOCKER_CREDENTIALS}") {
                        sh 'sudo docker push $DOCKER_IMAGE'
                    }
                }

                sshagent(['build-server-ssh']) {
                    sh '''
                    ssh -o StrictHostKeyChecking=no ubuntu@18.234.101.52 "
                        sudo docker pull $DOCKER_IMAGE &&
                        sudo docker stop simple-java-app || true &&
                        sudo docker rm simple-java-app || true &&
                        sudo docker run -d --name simple-java-app -p 80:8080 $DOCKER_IMAGE
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
