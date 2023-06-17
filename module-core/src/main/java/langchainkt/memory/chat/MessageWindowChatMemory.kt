package langchainkt.memory.chat

import java.util.Optional
import langchainkt.data.message.ChatMessage
import langchainkt.data.message.SystemMessage
import langchainkt.internal.Validators
import langchainkt.memory.ChatMemory
import langchainkt.store.memory.chat.ChatMemoryStore
import langchainkt.store.memory.chat.InMemoryChatMemoryStore
import org.slf4j.LoggerFactory

/**
 * This chat memory operates as a sliding window of [.maxMessages] messages.
 * It retains as many of the most recent messages as can fit into the window.
 * If there isn't enough space for a new message, the oldest one is discarded.
 *
 *
 * Once added, a [SystemMessage] is always retained.
 * Only one [SystemMessage] can be held at a time.
 * If a new [SystemMessage] with the same content is added, it is ignored.
 * If a new [SystemMessage] with different content is added, it replaces the previous one.
 *
 *
 * The state of chat memory is stored in [ChatMemoryStore].
 */
class MessageWindowChatMemory private constructor(
  builder: Builder
) : ChatMemory {
  private val id: Any
  private val maxMessages: Int
  private val store: ChatMemoryStore

  init {
    id = builder.id
    maxMessages = Validators.ensureGreaterThanZero(builder.maxMessages, "maxMessages")
    store = builder.store
  }

  override fun id(): Any {
    return id
  }

  override fun add(message: ChatMessage) {
    val messages = messages()
    if (message is SystemMessage) {
      val systemMessage = findSystemMessage(messages)
      if (systemMessage.isPresent) {
        if (systemMessage.get() == message) {
          return  // do not add the same system message
        } else {
          messages.remove(systemMessage.get()) // need to replace existing system message
        }
      }
    }
    messages.add(message)
    ensureCapacity(messages, maxMessages)
    store.updateMessages(id, messages)
  }

  override fun messages(): MutableList<ChatMessage> {
    val messages: MutableList<ChatMessage> = ArrayList(store.getMessages(id))
    ensureCapacity(messages, maxMessages)
    return messages
  }

  override fun clear() {
    store.deleteMessages(id)
  }

  class Builder {
    internal var id: Any = "default"
    internal var maxMessages: Int = 1
    internal var store: ChatMemoryStore = InMemoryChatMemoryStore()

    /**
     * @param id The ID of the [ChatMemory].
     * If not provided, a "default" will be used.
     * @return builder
     */
    fun id(id: Any): Builder {
      this.id = id
      return this
    }

    /**
     * @param maxMessages The maximum number of messages to retain.
     * @return builder
     */
    fun maxMessages(maxMessages: Int): Builder {
      this.maxMessages = maxMessages
      return this
    }

    /**
     * @param store The chat memory store responsible for storing the chat memory state.
     * If not provided, an [InMemoryChatMemoryStore] will be used.
     * @return builder
     */
    fun chatMemoryStore(store: ChatMemoryStore): Builder {
      this.store = store
      return this
    }

    fun build(): MessageWindowChatMemory {
      return MessageWindowChatMemory(this)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(MessageWindowChatMemory::class.java)

    private fun findSystemMessage(messages: List<ChatMessage>): Optional<SystemMessage> {
      return messages
        .filterIsInstance<SystemMessage>()
        .firstOrNull()
        .let { Optional.ofNullable(it) }
    }

    private fun ensureCapacity(messages: MutableList<ChatMessage>, maxMessages: Int) {
      while (messages.size > maxMessages) {
        var messageToRemove = 0
        if (messages[0] is SystemMessage) {
          messageToRemove = 1
        }
        val removedMessage: ChatMessage = messages.removeAt(messageToRemove)
        log.trace("Removing the following message to comply with the capacity requirements: {}", removedMessage)
      }
    }

    @JvmStatic
    fun builder(): Builder {
      return Builder()
    }

    @JvmStatic
    fun withMaxMessages(maxMessages: Int): MessageWindowChatMemory {
      return builder().maxMessages(maxMessages).build()
    }
  }
}
