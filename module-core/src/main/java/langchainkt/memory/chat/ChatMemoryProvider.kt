package langchainkt.memory.chat

import langchainkt.memory.ChatMemory

/**
 * Provides instances of [ChatMemory].
 * Intended to be used with [langchainkt.service.AiServices].
 */
interface ChatMemoryProvider {
  /**
   * Provides an instance of [ChatMemory].
   * This method is called each time an AI Service method (having a parameter annotated with [langchainkt.service.MemoryId])
   * is called with a previously unseen memory ID.
   * Once the [ChatMemory] instance is returned, it's retained in memory and managed by [langchainkt.service.AiServices].
   *
   * @param memoryId The ID of the chat memory.
   * @return A [ChatMemory] instance.
   */
  operator fun get(memoryId: Any): ChatMemory?
}
