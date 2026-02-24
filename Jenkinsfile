pipeline {
    agent any
    stages {
        stage('Deploy service') {
            steps {
                sh '''
                cd /opt/app
                git pull origin main
                docker compose --env-file /etc/finguard.env build app
                docker compose --env-file /etc/finguard.env up -d app
                '''
            }
        }
    }
}