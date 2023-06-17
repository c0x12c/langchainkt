package langchainkt.store.memory.chat

import java.util.concurrent.ConcurrentHashMap
import langchainkt.data.message.ChatMessage

/**
 * Implementation of [ChatMemoryStore] that stores state of [ChatMemory] (chat messages) in-memory.
 *
 *
 * This storage mechanism is transient and does not persist data across application restarts.
 */
class InMemoryChatMemoryStore : ChatMemoryStore {
  private val messagesByMemoryId: MutableMap<Any, List<ChatMessage>> = ConcurrentHashMap()

  override fun getMessages(memoryId: Any): List<ChatMessage> {
    return messagesByMemoryId.computeIfAbsent(memoryId) { ArrayList() }
  }

  override fun updateMessages(memoryId: Any, messages: List<ChatMessage>) {
    messagesByMemoryId[memoryId] = messages
  }

  override fun deleteMessages(memoryId: Any) {
    messagesByMemoryId.remove(memoryId)
  }
}
