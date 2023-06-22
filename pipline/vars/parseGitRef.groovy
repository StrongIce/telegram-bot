String call(String ref) {
    result = ''
    if (isTagRef(ref)) {
        result = ref.replaceAll('refs/tags/', '')
    }
    if (ref.startsWith('refs/heads')) {
        result = ref.replaceAll('refs/heads/', '')
    }
    return result
}
