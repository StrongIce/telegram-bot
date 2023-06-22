void call(String tag) {
    sh "git tag ${tag}"
    sh 'git push --tags'
}
