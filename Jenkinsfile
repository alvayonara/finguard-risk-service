pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'git@github.com:alvayonara/finguard-risk-service.git'
            }
        }

        stage('Deploy service') {
            steps {
                sh '''
                cd /opt/app
                git pull origin main
                docker compose --env-file /etc/finguard.env up -d --build app
                '''
            }
        }
    }
}