package langchainkt.data.message

interface ChatMessage {
  val text: String?
  val type: ChatMessageType

  fun text(): String {
    return text!!
  }
}
