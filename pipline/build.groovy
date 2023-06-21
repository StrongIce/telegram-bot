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
            setBuildStatus("Build succeeded", "SUCCESS");
        }
        failure {
            setBuildStatus("Build failed", "FAILURE");
        }
    }
}