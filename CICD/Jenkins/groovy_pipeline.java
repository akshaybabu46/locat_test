pipeline {
    agent any

    tools {
        jdk 'jdk17'
        nodejs 'node18'
    }

    environment {

        SONARQUBE = tool 'sonarqube-scanner'  
    }

    stages {

        stage('Clean Workspace') {
            steps {
                echo "Cleaning Workspace..."
                cleanWs()
            }
        }

        stage('Checkout from Git') {
            steps {                  
                echo "Chekking out from Git"
                git branch: 'main' , url: 'https://github.com/akshaybabu46/webgame.git'  
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo "Running SonarQube code analysis..."
                withSonarQubeEnv('SonarQube-Server') {
                    sh """
                        $SONARQUBE/bin/sonar-scanner \
                        -Dsonar.projectKey=guess-number-app \
                        -Dsonar.projectName=guess-number-app
                    """
                }
            }
        }

        stage('Install Dependencies') {
            steps {
                echo "Installing Node.js dependencies..."
                sh 'npm install'
            }
        }

        stage('Trivy FS Scan') {
            steps {
                echo "Scanning FS with Trivy..."
                sh '''
                    trivy fs . > trivyfs.txt
                '''
            }
        }

        stage("Docker build and push") {

            steps {
                script {
                    withDockerRegistry(credentialsId: 'dockerhub' , toolName: 'docker') {
                        sh "docker build -t guess-number ."
                        sh "docker tag guess-number akshaybabu46/guess-number:latest"
                        sh "docker push akshaybabu46/guess-number:latest"
                    }
                }
            }

        }


        stage('Trivy Image Scan') {
            steps {
                echo "Scanning Docker image with Trivy..."
                sh '''
                    trivy image --severity HIGH,CRITICAL --exit-code 0 --no-progress guess-number:latest
                '''
            }
        }

        stage('Deploy') {
            steps {
                echo "Running the container..."
                sh '''
                    docker stop guess-number || true
                    docker rm guess-number || true
                    docker run -d -p 3000:3000 --name guess-number guess-number:latest
                '''
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline completed successfully!"
        }
        failure {
            echo "❌ Pipeline failed. Check logs for details."
        }
    }
}
