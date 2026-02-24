pipeline {
    agent any
    stages {
        stage('Deploy service') {
            steps {
                sh '''
                set -e
                set -x

                cd /opt/app

                docker compose --env-file /etc/finguard.env build app
                docker compose --env-file /etc/finguard.env up -d app
                '''
            }
        }
    }
}