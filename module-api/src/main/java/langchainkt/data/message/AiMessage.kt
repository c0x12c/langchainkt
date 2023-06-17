package langchainkt.data.message

import langchainkt.agent.tool.ToolExecutionRequest

/**
 * Represents a response message from an AI (language model).
 * The message can contain either a textual response or a request to execute a tool.
 * In the case of tool execution, the response to this message should be a [ToolExecutionResultMessage].
 */
data class AiMessage(
  override val text: String?,
  private val toolExecutionRequest: ToolExecutionRequest? = null
) : ChatMessage {

  fun toolExecutionRequest(): ToolExecutionRequest? {
    return toolExecutionRequest
  }

  override val type: ChatMessageType = ChatMessageType.AI

  companion object {
    fun from(text: String): AiMessage {
      return AiMessage(text)
    }

    fun from(toolExecutionRequest: ToolExecutionRequest?): AiMessage {
      return AiMessage(null, toolExecutionRequest)
    }

    @JvmStatic
    fun aiMessage(text: String): AiMessage {
      return from(text)
    }

    @JvmStatic
    fun aiMessage(toolExecutionRequest: ToolExecutionRequest?): AiMessage {
      return from(toolExecutionRequest)
    }
  }
}
