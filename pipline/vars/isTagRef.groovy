Boolean call(String ref) {
    return ref.startsWith('refs/tags') ? true : false
}
