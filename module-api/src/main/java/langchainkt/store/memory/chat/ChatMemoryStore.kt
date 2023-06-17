package langchainkt.store.memory.chat

import langchainkt.data.message.ChatMessage

/**
 * Represents a store for the [ChatMemory] state.
 * Allows for flexibility in terms of where and how chat memory is stored.
 * Currently, the only implementation available is [InMemoryChatMemoryStore]. We are in the process of adding
 * ready implementations for popular stores like SQL DBs, document stores, etc.
 * In the meantime, you can implement this interface to connect to any storage of your choice.
 */
interface ChatMemoryStore {
  /**
   * Retrieves messages for a specified chat memory.
   *
   * @param memoryId The ID of the chat memory.
   * @return List of messages for the specified chat memory. Must not be null. Can be deserialized from JSON using [ChatMessageDeserializer].
   */
  fun getMessages(memoryId: Any): List<ChatMessage>

  /**
   * Updates messages for a specified chat memory.
   *
   * @param memoryId The ID of the chat memory.
   * @param messages List of messages for the specified chat memory, that represent the current state of the [ChatMemory].
   * Can be serialized to JSON using [ChatMessageSerializer].
   */
  fun updateMessages(memoryId: Any, messages: List<ChatMessage>)

  /**
   * Deletes all messages for a specified chat memory.
   *
   * @param memoryId The ID of the chat memory.
   */
  fun deleteMessages(memoryId: Any)
}
