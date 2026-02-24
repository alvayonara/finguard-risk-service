pipeline {
    agent any
    environment {
        COMPOSE_FILE = "docker-compose.yml"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build Maven') {
            steps {
                sh 'mvn clean package -DskipTests -Dpit.skip=true'
            }
        }
        stage('Build Docker Image') {
            steps {
                sh 'docker compose build'
            }
        }
        stage('Deploy') {
            steps {
                sh 'docker compose up -d'
            }
        }
    }

    post {
        success {
            echo 'Deployment done.'
        }
        failure {
            echo 'Deployment failed.'
        }
    }
}