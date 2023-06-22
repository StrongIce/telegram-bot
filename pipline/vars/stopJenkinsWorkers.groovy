/* groovylint-disable LineLength */
void call() {
    sh '''
        if [[ "$(/var/lib/jenkins/yandex-cloud/bin/yc compute instance get st-jenkins-worker --format json | jq -Mcr '.status')" != "STOPPED" ]];
        then
            /var/lib/jenkins/yandex-cloud/bin/yc compute instance stop st-jenkins-worker
        fi
    '''
}
