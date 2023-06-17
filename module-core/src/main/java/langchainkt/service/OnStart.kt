package langchainkt.service

interface OnStart {
  /**
   * Invoke this method to send a request to LLM and start response streaming.
   */
  fun start()
}
