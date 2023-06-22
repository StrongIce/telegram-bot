/* groovylint-disable LineLength */
void call() {
    sh '''
        if [[ "$(/var/lib/jenkins/yandex-cloud/bin/yc compute instance get st-jenkins-worker --format json | jq -Mc '.status')" != "\\"RUNNING\\"" ]];
        then
            /var/lib/jenkins/yandex-cloud/bin/yc compute instance start st-jenkins-worker
        fi
    '''
}
