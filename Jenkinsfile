pipeline {
    agent any

    stages {
        stage('Deploy') {
            steps {
                sh '''
                cd $WORKSPACE
                docker compose up -d --build
                '''
            }
        }
    }
}