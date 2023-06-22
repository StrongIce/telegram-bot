void call(String credentialsId) {
  withCredentials([
    file(credentialsId: credentialsId, variable: 'KEY_FILE')
  ]) {
    sh """
      cat \$KEY_FILE | docker login \
      --username json_key \
      --password-stdin \
      ${getYandexRegistryAddr()}
    """
  }
}
