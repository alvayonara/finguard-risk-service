pipeline {
    agent any

    stages {

        stage('Test') {
                steps {
                    sh 'echo HELLO'
                }
            }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Deploy service') {
            steps {
                sh '''
                set -e
                set -x

                cd /opt/app
                git fetch origin
                git reset --hard origin/main

                docker compose --env-file /etc/finguard.env build app
                docker compose --env-file /etc/finguard.env up -d app
                '''
            }
        }
    }
}