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
                git branch: 'main' , url: 'https://github.com/akshaybabu46/swiggy-clone.git'  
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo "Running SonarQube code analysis..."
                withSonarQubeEnv('SonarQube-Server') {
                    sh """
                        $SONARQUBE/bin/sonar-scanner \
                        -Dsonar.projectKey=swiggy-clone-app \
                        -Dsonar.projectName=swiggy-clone-app
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
                        sh "docker build -t swiggy-clone ."
                        sh "docker tag swiggy-clone akshaybabu46/swiggy-clone:latest"
                        sh "docker push akshaybabu46/swiggy-clone:latest"
                    }
                }
            }

        }


        stage('Trivy Image Scan') {
            steps {
                echo "Scanning Docker image with Trivy..."
                sh '''
                    trivy image --severity HIGH,CRITICAL --exit-code 0 --no-progress swiggy-clone:latest
                '''
            }
        }

        stage('Deploy') {
            steps {
                echo "Running the container..."
                sh '''
                    docker stop swiggy-clone || true
                    docker rm swiggy-clone || true
                    docker run -d -p 3001:3000 --name swiggy-clone swiggy-clone:latest
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
