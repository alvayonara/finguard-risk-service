pipeline {
    agent any

    stages {
        stage('Deploy') {
            steps {
                sh '''
                docker compose down || true
                docker compose up -d --build
                '''
            }
        }
    }
}