pipeline {
    agent any

    environment {
        IMAGE_NAME = "finguard-risk-service-app"
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timestamps()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                sh '''
                echo "Building Docker image: $IMAGE_NAME:$IMAGE_TAG"

                docker build \
                  -t $IMAGE_NAME:$IMAGE_TAG \
                  -t $IMAGE_NAME:latest \
                  .
                '''
            }
        }

        stage('Verify Image') {
            steps {
                sh '''
                echo "Available images:"
                docker images | grep $IMAGE_NAME || true
                '''
            }
        }
    }

    post {
        success {
            echo "CI build succeed"
            echo "Built image: ${IMAGE_NAME}:${IMAGE_TAG}"
        }

        failure {
            echo "CI build failed"
        }
    }
}