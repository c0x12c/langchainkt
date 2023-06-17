package langchainkt.model.input

import langchainkt.data.message.AiMessage
import langchainkt.data.message.SystemMessage
import langchainkt.data.message.UserMessage
import langchainkt.internal.Utils

/**
 * Represents a prompt (an input text sent to the LLM).
 * A prompt usually contains instructions, contextual information, end-user input, etc.
 * A Prompt is typically created by applying one or multiple values to a PromptTemplate.
 */
data class Prompt(
  private val text: String
) {

  init {
    require(text.isNotEmpty()) { "Prompt text must not be empty" }
  }

  fun text(): String {
    return text
  }

  fun toSystemMessage(): SystemMessage {
    return SystemMessage.systemMessage(text)
  }

  fun toUserMessage(): UserMessage {
    return UserMessage.userMessage(text)
  }

  fun toAiMessage(): AiMessage {
    return AiMessage.aiMessage(text)
  }

  override fun toString(): String {
    return "Prompt {" +
      " text = " + Utils.quoted(text) +
      " }"
  }

  companion object {
    @JvmStatic
    fun from(text: String): Prompt {
      return Prompt(text)
    }
  }
}
