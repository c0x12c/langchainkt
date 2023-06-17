package langchainkt.data.message

import kotlin.reflect.KClass

enum class ChatMessageType(
  val type: KClass<out ChatMessage>
) {
  SYSTEM(SystemMessage::class),
  USER(UserMessage::class),
  AI(AiMessage::class),
  TOOL_EXECUTION_RESULT(ToolExecutionResultMessage::class);


  override fun toString(): String {
    return name.uppercase()
  }
}


