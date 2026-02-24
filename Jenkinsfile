pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Deploy') {
            steps {
                sh '''
                set -e
                set -x

                cd $WORKSPACE
                docker-compose --env-file /etc/finguard.env build app
                docker-compose --env-file /etc/finguard.env up -d app
                '''
            }
        }
    }
}