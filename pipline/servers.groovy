/* groovylint-disable DuplicateNumberLiteral, DuplicateStringLiteral, LineLength, NestedBlockDepth, UnnecessaryGetter */
library('automatization')

// This var will contain result to checkout.
versionRef = 'unknown'
// Is build triggered by webhook.
triggeredByWebhook = false

properties([
    parameters([
        string(
            name: 'environment',
            defaultValue: '',
            description: 'Project name'
        ),
    ]),
])

repoDir = 'automatization'

allowedBranchesToBuildWebhook = [ 'main' ]
allowedEnvironmentsRepoToBuildWebhook = [ 'ansible.aws.lightsail' ]

// Сопоставление env-файла compose файлам
envFileToComposeName = [
    'envs/test-env/.env': [
        'envs/test-env/docker-compose.yaml',
        'envs/test-env/docker-compose-unity.yaml'
    ],
    'envs/dev-env/.env': [
        'envs/dev-env/docker-compose.yaml',
        'envs/dev-env/docker-compose-unity.yaml'
    ],
    'envs/demo-env/.env': [
        'envs/demo-env/docker-compose.yaml',
        'envs/demo-env/docker-compose-unity.yaml'
    ],
    'envs/prod-env/.env': [
        'envs/prod-env/docker-compose.yaml',
        'envs/prod-env/docker-compose-unity.yaml'
    ],
]

// Сопоставление compose-файла имени сервера, на котором он развернут
composeFileToVMName = [
    'envs/test-env/docker-compose.yaml': 'test-backend-service',
    'envs/test-env/docker-compose-unity.yaml': 'test-unity-service',
    'envs/dev-env/docker-compose.yaml': 'dev-server',
    'envs/dev-env/docker-compose-unity.yaml': 'dev-server-unity',
    'envs/demo-env/docker-compose.yaml': 'demo-unity-server',
    'envs/demo-env/docker-compose-unity.yaml': 'demo-server-unity',
    'envs/prod-env/docker-compose.yaml': 'prod-server',
    'envs/prod-env/docker-compose-unity.yaml': 'prod-server-unity',
]

user = 'auto'

pipeline {
    agent {
        label 'master'
    }
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
            token: 'automatization',
            tokenCredentialId: ''
        )
    }
    options {
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '100'))
    }
    stages {
        // Этап, определяющий основные параметры сборки - кто и что запустил
        stage('Check params') {
            steps {
                script {
                    // sh 'env'
                    description = '''
                        <ul style="margin:0px;padding-left:10px">
                    '''.stripIndent().trim()
                    // Если необходимо прервать сборку после проверки параметров
                    // Ставится в true
                    buildAborted = false
                    buildAbortedMsg = 'This commit'

                    wrap([$class: 'BuildUser']) {
                        try {
                            user = BUILD_USER
                        } catch (e) {
                            echo 'User not in scope, probably triggered from another job'
                        }
                    }

                    // Если сборка запускается вебхуком -
                    // определяюются переменные окружения.
                    // Проверяем что запустились с события push
                    if (env.GITHUB_REF) {
                        // Определяем ветку/тег с которого собирается
                        parsedRef = parseGitRef(env.GITHUB_REF)
                        parsedRepo = env.GITHUB_REPO_NAME
                        // Если сборка с ветки и ветка не находится в
                        // списке разрешенных - останавливаем сборку
                        if (!(parsedRef in allowedBranchesToBuildWebhook) && !(parsedRepo in allowedEnvironmentsRepoToBuildWebhook) && !(isTagRef(env.GITHUB_REF))) {
                            buildAborted = true
                            buildAbortedMsg = parsedRef
                        }
                        // Определяем версию - это либо тег, либо первые 8 символов коммит-хэша
                        versionRef = isTagRef(env.GITHUB_REF) ? parsedRef : env.GITHUB_COMMIT_HASH.take(8)
                        triggeredByWebhook = true
                    } else {
                        // Если запускаем из jenkins или по другой причине -
                        // берем ветку main
                        versionRef = 'main'
                    }
                    description += """
                    <li>Version: ${versionRef}</li>
                    """.stripIndent().trim()

                    if (env.GITHUB_COMMIT_AUTHOR_NAME) {
                        description += """
                        <li>User: ${env.GITHUB_COMMIT_AUTHOR_NAME}</li>
                        """.stripIndent().trim()
                    } else {
                        description += """
                        <li>User: ${user}</li>
                        """.stripIndent().trim()
                    }
                    if (env.GITHUB_COMMIT_AUTHOR_EMAIL) {
                        description += """
                        <li>Email: ${env.GITHUB_COMMIT_AUTHOR_EMAIL}</li>
                        """.stripIndent().trim()
                    }

                    description += '''
                    </ul>
                    '''.stripIndent().trim()

                    // Определяем название сборки и описание
                    currentBuild.description = description
                    /* groovylint-disable-next-line UnnecessaryGetter */
                    currentBuild.displayName = "#${env.BUILD_ID}-${getDate()}-${versionRef}"

                    // Отмена сборки
                    if (buildAborted) {
                        currentBuild.result = 'ABORTED'
                        error("${buildAbortedMsg} is not allowed to build by webhook")
                    }
                }
            }
        }
        stage("test"){
            steps {
                script {
                    sh "echo WORRRRRRLD HELOOOOOOO"
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