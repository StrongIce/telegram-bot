String call(String suffix = '') {
    /* groovylint-disable-next-line UnnecessaryGetter */
    v = "${getDate()}.${env.BUILD_ID}"
    if (suffix) {
        v = "${v}.${suffix}".replace('/', '-')
    }
    return v
}
