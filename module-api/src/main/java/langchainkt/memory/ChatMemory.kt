package langchainkt.memory

import langchainkt.data.message.ChatMessage

/**
 * Represents the memory (history) of a chat conversation.
 * Since language models do not keep the state of the conversation, it is necessary to provide all previous messages
 * on every interaction with the language model.
 * [ChatMemory] helps with keeping track of the conversation and ensuring that messages fit within language model's context window.
 */
interface ChatMemory {
  /**
   * @return The ID of the [ChatMemory].
   */
  fun id(): Any

  /**
   * Adds a message to the chat memory.
   *
   * @param message The [ChatMessage] to add.
   */
  fun add(message: ChatMessage)

  /**
   * Retrieves messages from the chat memory.
   * Depending on the implementation, it may not return all previously added messages,
   * but rather a subset, a summary, or a combination thereof.
   *
   * @return A list of [ChatMessage] objects that represent the current state of the chat memory.
   */
  fun messages(): MutableList<ChatMessage>

  /**
   * Clears the chat memory.
   */
  fun clear()
}
