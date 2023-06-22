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
        GenericTrigger(
            causeString: 'Generic Cause',
            genericVariables: [
                [
                    defaultValue: '',
                    key: 'GITHUB_REF',
                    regexpFilter: '',
                    value: '$.ref'
                ],
                [
                    defaultValue: '',
                    key: 'GITHUB_COMMIT_AUTHOR_NAME',
                    regexpFilter: '',
                    value: '$.head_commit.author.name'
                ],
                [
                    defaultValue: '',
                    key: 'GITHUB_COMMIT_AUTHOR_EMAIL',
                    regexpFilter: '',
                    value: '$.head_commit.author.email'
                ],
                [
                    defaultValue: '',
                    key: 'GITHUB_COMMIT_HASH',
                    regexpFilter: '',
                    value: '$.head_commit.id'
                ],
                [
                    defaultValue: '',
                    key: 'GITHUB_REPO_NAME',
                    regexpFilter: '',
                    value: '$.repository.name'
                ],
                [
                    defaultValue: '[]',
                    key: 'GITHUB_COMMITS',
                    regexpFilter: '',
                    value: '$.commits'
                ]
            ],
            regexpFilterExpression: '',
            regexpFilterText: '',
            token: 'tzhenguldinov',
            tokenCredentialId: ''
        )
    }

    stages {

        stage('Hello') {
            steps {
                script {
                    sh 'echo $pwd'
                    sh "echo ${env.GITHUB_REPO_NAME}"
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
                        servicesToUpdate = dockerServicesByProject[params.project]
                        url = 'git@github.com:StrongIce/ansible.aws.lightsail.git'
                        if (CURRENT_BRANCH_NAME =~ /(.*\/main)|(^main)(?=\s|$)/) {
                            updateComposeFile = '.env'
                        }
                        if (CURRENT_BRANCH_NAME =~ /(.*\/master)|(^master)(?=\s|$)/) {
                            updateComposeFile = '.env'
                        }
                        if (updateComposeFile) {
                            build(
                                job: 'tzhenguldinov/update.service',
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
