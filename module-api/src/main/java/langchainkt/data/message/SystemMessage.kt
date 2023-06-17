package langchainkt.data.message

/**
 * Represents a system message, typically defined by a developer.
 * This type of message usually provides instructions regarding the AI's actions, such as its behavior or response style.
 */
data class SystemMessage(
  override val text: String
) : ChatMessage {

  override val type: ChatMessageType = ChatMessageType.SYSTEM

  companion object {
    fun from(text: String): SystemMessage {
      return SystemMessage(text)
    }

    @JvmStatic
    fun systemMessage(text: String): SystemMessage {
      return from(text)
    }
  }
}
