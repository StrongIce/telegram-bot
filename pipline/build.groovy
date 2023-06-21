library('test-library')

properties([
    parameters([
        string(
            name: 'project',
            defaultValue: 'telegram-bot',
            description: 'Project name'
        )
    ]),
])



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
