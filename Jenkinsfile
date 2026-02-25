pipeline {
    agent any

    stages {
        stage('Deploy') {
            steps {
                dir("${env.WORKSPACE}") {
                    sh 'docker compose up -d --build'
                }
            }
        }
    }
}