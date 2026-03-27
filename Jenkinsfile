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

        stage('6. OWASP Dependency Check (Docker)') {
            steps {
                script {
                    docker.image('owasp/dependency-check:12.3.0').inside {
                        sh '''
                            dependency-check.sh \
                                --project "simple-java-app" \
                                --scan ./target \
                                --format "HTML" \
                                --format "JSON" \
                                --format "XML" \
                                --out dependency-check-report \
                                --nvd-api-key $NVD_API_KEY
                        '''
                    }
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
                    docker.withRegistry('https://index.docker.io/v1/', "${DOCKER_CREDENTIALS}") {
                        sh 'docker push $DOCKER_IMAGE'
                    }
                }

                sshagent(['build-server-ssh']) {
                    sh '''
                    ssh -o StrictHostKeyChecking=no ubuntu@18.234.101.52 "
                        docker pull $DOCKER_IMAGE &&
                        docker stop simple-java-app || true &&
                        docker rm simple-java-app || true &&
                        docker run -d --name simple-java-app -p 80:8080 $DOCKER_IMAGE
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
