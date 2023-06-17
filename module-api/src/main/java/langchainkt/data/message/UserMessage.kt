package langchainkt.data.message

import java.util.Objects
import langchainkt.internal.Utils

/**
 * Represents a message from a user, typically an end user of the application.
 */
class UserMessage(
  val name: String?,
  override val text: String?,
) : ChatMessage {

  constructor(text: String?) : this(null, text)

  fun name(): String? {
    return name
  }

  override val type: ChatMessageType = ChatMessageType.USER

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val that = other as UserMessage
    return name == that.name && text == that.text
  }

  override fun hashCode(): Int {
    return Objects.hash(name, text)
  }

  override fun toString(): String {
    return "UserMessage {" +
      " name = " + Utils.quoted(name) +
      " text = " + Utils.quoted(text) +
      " }"
  }

  companion object {
    @JvmStatic
    fun from(text: String?): UserMessage {
      return UserMessage(text)
    }

    fun from(name: String?, text: String?): UserMessage {
      return UserMessage(name, text)
    }

    @JvmStatic
    fun userMessage(text: String?): UserMessage {
      return from(text)
    }

    @JvmStatic
    fun userMessage(name: String?, text: String?): UserMessage {
      return from(name, text)
    }
  }
}
