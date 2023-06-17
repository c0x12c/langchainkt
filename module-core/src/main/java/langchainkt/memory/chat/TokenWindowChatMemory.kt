package langchainkt.memory.chat

import java.util.Optional
import langchainkt.data.message.ChatMessage
import langchainkt.data.message.SystemMessage
import langchainkt.internal.Validators
import langchainkt.memory.ChatMemory
import langchainkt.model.Tokenizer
import langchainkt.store.memory.chat.ChatMemoryStore
import langchainkt.store.memory.chat.InMemoryChatMemoryStore
import org.slf4j.LoggerFactory

/**
 * This chat memory operates as a sliding window of [.maxTokens] tokens.
 * It retains as many of the most recent messages as can fit into the window.
 * If there isn't enough space for a new message, the oldest one (or multiple) is discarded.
 * Messages are indivisible. If a message doesn't fit, it's discarded completely.
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
class TokenWindowChatMemory private constructor(builder: Builder) : ChatMemory {
  private val id: Any
  private val maxTokens: Int
  private val tokenizer: Tokenizer
  private val store: ChatMemoryStore

  init {
    id = builder.id
    maxTokens = Validators.ensureGreaterThanZero(builder.maxTokens, "maxTokens")
    tokenizer = builder.tokenizer
    store = builder.store
  }

  override fun id(): Any {
    return id
  }

  override fun add(message: ChatMessage) {
    val messages = messages()
    if (message is SystemMessage) {
      val maybeSystemMessage = findSystemMessage(messages)
      if (maybeSystemMessage.isPresent) {
        if (maybeSystemMessage.get() == message) {
          return  // do not add the same system message
        } else {
          messages.remove(maybeSystemMessage.get()) // need to replace existing system message
        }
      }
    }
    messages.add(message)
    ensureCapacity(messages, maxTokens, tokenizer)
    store.updateMessages(id, messages)
  }

  override fun messages(): MutableList<ChatMessage> {
    val messages: MutableList<ChatMessage> = ArrayList(store.getMessages(id))
    ensureCapacity(messages, maxTokens, tokenizer)
    return messages
  }

  override fun clear() {
    store.deleteMessages(id)
  }

  class Builder {
    internal var id: Any = "default"
    internal var maxTokens: Int = 1
    internal lateinit var tokenizer: Tokenizer
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
     * @param maxTokens The maximum number of tokens to retain.
     * Chat memory will retain as many of the most recent messages as can fit into `maxTokens`.
     * Messages are indivisible. If a message doesn't fit, it's discarded completely.
     * @param tokenizer A [Tokenizer] responsible for counting tokens in the messages.
     * @return builder
     */
    fun maxTokens(maxTokens: Int, tokenizer: Tokenizer): Builder {
      this.maxTokens = maxTokens
      this.tokenizer = tokenizer
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

    fun build(): TokenWindowChatMemory {
      return TokenWindowChatMemory(this)
    }
  }

  companion object {
    private val log = LoggerFactory.getLogger(TokenWindowChatMemory::class.java)
    private fun findSystemMessage(messages: List<ChatMessage>): Optional<SystemMessage> {
      return messages.stream()
        .filter { message: ChatMessage? -> message is SystemMessage }
        .map { message: ChatMessage -> message as SystemMessage }
        .findAny()
    }

    private fun ensureCapacity(messages: MutableList<ChatMessage>, maxTokens: Int, tokenizer: Tokenizer) {
      var currentTokenCount = tokenizer.estimateTokenCountInMessages(messages)
      while (currentTokenCount > maxTokens) {
        var messageToRemove = 0
        if (messages[0] is SystemMessage) {
          messageToRemove = 1
        }
        val removedMessage: ChatMessage = messages.removeAt(messageToRemove)
        val tokenCountOfRemovedMessage = tokenizer.estimateTokenCountInMessage(removedMessage)
        log.trace("Removing the following message ({} tokens) to comply with the capacity requirements: {}",
          tokenCountOfRemovedMessage, removedMessage)
        currentTokenCount -= tokenCountOfRemovedMessage
      }
    }

    fun builder(): Builder {
      return Builder()
    }

    @JvmStatic
    fun withMaxTokens(maxTokens: Int, tokenizer: Tokenizer): TokenWindowChatMemory {
      return builder().maxTokens(maxTokens, tokenizer).build()
    }
  }
}
