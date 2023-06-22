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

dockerServicesByProject = [
    'telegram-bot':'vr-discovery',

]

pipeline {
    agent any

    triggers {
        githubPush()
    }

    stages {

        stage('Hello') {
            steps {
                script {
                    sh 'echo $pwd'
                }
            }
        }


        stage('Update compose') {
            steps {
                script {
                    dir(params.project) {
                        // Получаем список веток для данного коммита
                        CURRENT_BRANCH_NAME = sh(returnStdout: true, script: "git branch -a --contains \$(git rev-parse HEAD)").trim()
                        echo("CURRENT_BRANCH_NAME is ${CURRENT_BRANCH_NAME}")
                        updateComposeFile = ''
                        // Если список веток содержит main или master -
                        // вызываем автоматическое обновление файла
                        // envs/dev-env/.env
                        fieldsToUpdate = 'image'
                        valuesToUpdate = 'tutut'
                        servicesToUpdate = 'telegram-bot'
                        url = 'git@github.com:StrongIce/ansible.aws.lightsail.git'
                        if (CURRENT_BRANCH_NAME =~ /(.*\/main)|(^main)(?=\s|$)/) {
                            updateComposeFile = '.env'
                        }
                        if (CURRENT_BRANCH_NAME =~ /(.*\/master)|(^master)(?=\s|$)/) {
                            updateComposeFile = '.env'
                        }
                        if (updateComposeFile) {
                            build(
                                job: 'pipline/update',
                                wait: false,
                                propagate: true,
                                parameters: [
                                    string(name: 'file', value: updateComposeFile),
                                    string(name: 'services', value: servicesToUpdate),
                                    string(name: 'fields', value: fieldsToUpdate),
                                    string(name: 'values', value: valuesToUpdate),
                                    string(name: 'repo_url', value: url),
                                ]
                            )
                        }
                    }
                }
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
