package langchainkt.data.message

/**
 * Represents the result of a tool execution. Tool execution requests come from a previous AiMessage.
 */
data class ToolExecutionResultMessage(
  val toolName: String,
  override val text: String?,
) : ChatMessage {

  override val type: ChatMessageType = ChatMessageType.TOOL_EXECUTION_RESULT

  companion object {
    fun from(toolName: String, toolExecutionResult: String? = null): ToolExecutionResultMessage {
      return ToolExecutionResultMessage(toolName, toolExecutionResult)
    }

    @JvmStatic
    fun toolExecutionResultMessage(toolName: String, toolExecutionResult: String? = null): ToolExecutionResultMessage {
      return from(toolName, toolExecutionResult)
    }
  }
}
