package langchainkt.exception

class IllegalConfigurationException(
  message: String?
) : RuntimeException(message) {

  companion object {
    @JvmStatic
    fun illegalConfiguration(message: String?): IllegalConfigurationException {
      return IllegalConfigurationException(message)
    }

    @JvmStatic
    fun illegalConfiguration(format: String?, vararg args: Any?): IllegalConfigurationException {
      return IllegalConfigurationException(String.format(format!!, *args))
    }
  }
}
