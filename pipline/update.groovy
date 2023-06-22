/* groovylint-disable DuplicateMapLiteral, DuplicateNumberLiteral, DuplicateStringLiteral, LineLength, NestedBlockDepth, NestedForLoop, UnnecessaryGetter */
library('test-library')

/*
 * Этот пайплайн обновляет .env файл
 * Ожидается что переменные имеют вид ${service}_${var} и все '-'
 * именах env заменены на '_'
 */

// This var will contain result to checkout.
versionRef = 'unknown'
// Is build triggered by webhook.
triggeredByWebhook = false

automatizationSSHSecret = 'automatization-repo-ssh-key'
repoDir = 'automatization'
user = 'auto'

allowedBranchesToBuildWebhook = [ 'main' ]

properties([
    parameters([
        string(
            name: 'file',
            defaultValue: '',
            description: 'Compose environment file name'
        ),
        string(
            name: 'services',
            defaultValue: '',
            description: 'Services to update (Must be splitted by ,)'
        ),
        string(
            name: 'fields',
            description: 'service field names to update (Must be splitted by ,). Only string/num fields supported',
            defaultValue: '',
        ),
        string(
            name: 'values',
            description: 'service values for field (Must be splitted by ,). In order: values for fields service1,...values for fields serviceN',
            defaultValue: '',
        ),
        string(
            name: 'repo_url',
            description: 'service values for field (Must be splitted by ,). In order: values for fields service1,...values for fields serviceN',
            defaultValue: '',
        ),        
    ]),
])

pipeline {
    agent {
        label 'master'
    }
    options {
        // Отключаем параллельные сборки - для коммитов нужно обеспечить
        // максимальную атомарность действий
        disableConcurrentBuilds()
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '100'))
    }
    stages {
        // Этап, проверяющий основные параметры сборки
        stage('Check params') {
            steps {
                script {
                    if (!params.file) { error 'file to update is not defined' }
                    if (!params.services) { error 'services to update is not defined' }
                    wrap([$class: 'BuildUser']) {
                        try {
                            user = BUILD_USER
                        } catch (e) {
                            echo 'User not in scope, probably triggered from another job'
                        }
                    }
                    description = """
                        <ul style="margin:0px;padding-left:10px">
                        <li>File: ${params.file}</li>
                        <li>User: ${user}</li>
                        </ul>
                    """.stripIndent().trim()
                    currentBuild.description = description
                }
            }
        }

        // Применение изменений compose файла
        stage('Processing file') {
            steps {
                script {
                    dir(repoDir) {
                        sshagent(credentials: [automatizationSSHSecret]) {
                            // Клонирование/пулл изменений проекта Automatization
                            sh "git clone ${params.repo_url} ."
                            // Ставим автора коммита как jenkins
                            sh "git config --replace-all user.name 'jenkins'"
                            sh 'git config --replace-all user.email jenkins@sensecapital.vc'
                            services = params.services.split(',')
                            fields = params.fields.split(',')
                            values = params.values.split(',')
                            skipsrv = false
                            // Если мы вдруг обновляем env для win клиента.
                            // Его файл содержит только название архива.
                            // Записываем файл и скипаем всю дальнейшую логику.
                            if (params.file.endsWith('sense-tower-win-client.env')) {
                                v = sh(
                                    returnStdout: true,
                                    script: "echo ${values[0]} > ${params.file}"
                                ).trim()
                                skipsrv = true
                            }
                            if (!skipsrv) {
                                if (values.size() != fields.size() * services.size()) {
                                    error 'Fields count != values count'
                                }
                                for (int i = 0; i < services.size(); i++) {
                                    for (int j = 0; j < fields.size(); j++) {
                                        v = sh(
                                            returnStdout: true,
                                            script: "awk -F '=' '/${services[i].replace('-', '_')}_${fields[j].replace('-', '_')}/ {print \$2}' ${params.file}"
                                        ).trim()
                                        if (v != '') {
                                            sh """
                                                sed -i 's/^\\(${services[i].replace('-', '_')}_${fields[j].replace('-', '_')}=\\).*/\\1${values[j + i * fields.size()].replace('/', '\\/')}/' ${params.file}
                                            """.stripIndent().trim()
                                            println "Service ${services[i]} update field ${fields[j]} from ${v} to ${values[j + i * fields.size()]}"
                                        } else {
                                            println "Value ${fields[j]} for service ${services[i]} does not exist and cannot be updated"
                                        }
                                    }
                                }
                            }
                            checkDiff = sh(script: 'git diff --exit-code --quiet --cached && git diff --exit-code --quiet', returnStatus: true)
                            if (checkDiff == 0) {
                                println 'Nothing to commit'
                            } else {
                                sh "git commit -am 'Update: ${params.file} updated services ${params.services}, build ${env.BUILD_ID} by ${user}'"
                                sh 'git push'
                            }
                        }
                    }
                }
            }
        }
    }
    post {
        cleanup {
            cleanWs()
        }
    }
}