library('test-library')

pipeline {
    agent any

    triggers {
        githubPush()
    }

    stages {

        stage('Hello') {
            steps {
                echo 'Hello World'
            }
        }

    }

    post {
        success {
            buildStatus("Build succeeded", "SUCCESS");
        }
        failure {
            buildStatus("Build failed", "FAILURE");
        }
    }
}