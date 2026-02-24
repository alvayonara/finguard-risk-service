pipeline {
    agent any

    stages {

        stage('Debug Environment') {
            steps {
                sh '''
                set -x
                echo "WHOAMI:"
                whoami || true
                echo "PWD:"
                pwd || true
                echo "WORKSPACE:"
                echo $WORKSPACE || true
                echo "LIST WORKSPACE:"
                ls -la || true
                echo "CHECK ENV FILE:"
                ls -la /etc/finguard.env || true
                echo "CHECK DOCKER:"
                which docker || true
                docker --version || true
                echo "CHECK DOCKER-COMPOSE:"
                which docker-compose || true
                docker-compose --version || true
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                set -ex
                cd $WORKSPACE
                docker-compose --env-file /etc/finguard.env build app
                docker-compose --env-file /etc/finguard.env up -d app
                '''
            }
        }
    }
}