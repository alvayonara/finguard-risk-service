pipeline {
    agent any
    environment {
        IMAGE_NAME = "finguard-risk-service-app"
        IMAGE_TAG  = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Image') {
            steps {
                sh '''
                docker build -t $IMAGE_NAME:$IMAGE_TAG .
                docker tag $IMAGE_NAME:$IMAGE_TAG $IMAGE_NAME:latest
                '''
            }
        }

        stage('Approve Deploy') {
            steps {
                input message: "Deploy version ${IMAGE_TAG} to PRODUCTION?"
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                docker compose down || true
                IMAGE_TAG=$IMAGE_TAG docker compose up -d
                '''
            }
        }
    }
}