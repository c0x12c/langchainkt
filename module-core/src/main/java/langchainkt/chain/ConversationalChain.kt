package langchainkt.chain

import langchainkt.data.message.UserMessage
import langchainkt.internal.Validators
import langchainkt.memory.ChatMemory
import langchainkt.memory.chat.MessageWindowChatMemory
import langchainkt.model.chat.ChatLanguageModel

/**
 * A chain for interacting with a specified [ChatLanguageModel] while maintaining a memory of the conversation.
 * Includes a default [ChatMemory] (a message window with maximum 10 messages), which can be overridden.
 */
class ConversationalChain(
  private val chatLanguageModel: ChatLanguageModel,
  private val chatMemory: ChatMemory = MessageWindowChatMemory.withMaxMessages(10)
) : Chain<String, String> {

  override fun execute(input: String): String {
    chatMemory.add(UserMessage.userMessage(Validators.ensureNotBlank(input, "userMessage")))
    val message = chatLanguageModel.generate(chatMemory.messages()).content()
    chatMemory.add(message)
    return message.text()
  }
}
