pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build & Deploy Docker') {
            steps {
                sh '''
                docker compose \
                  --env-file /etc/finguard.env \
                  up -d --build
                '''
            }
        }
    }

    post {
        success {
            echo 'Deployment done'
        }
        failure {
            echo 'Deployment failed'
        }
    }
}