package langchainkt.data.message

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class ChatMessageJsonTest {

  @Test
  fun `UserMessage round-trip`() {
    val message = UserMessage.userMessage("hello")
    val json = ChatMessageJson.serialize(message)
    val result = ChatMessageJson.deserialize<UserMessage>(json)
    expectThat(result).isEqualTo(message)
  }

  @Test
  fun `SystemMessage round-trip`() {
    val message = SystemMessage.from("hello")
    val json = ChatMessageJson.serialize(message)
    val result = ChatMessageJson.deserialize<SystemMessage>(json)
    expectThat(result).isEqualTo(message)
  }

  @Test
  fun `AiMessage round-trip`() {
    val message = AiMessage.from("hello")
    val json = ChatMessageJson.serialize(message)
    val result = ChatMessageJson.deserialize<AiMessage>(json)
    expectThat(result).isEqualTo(message)
  }

  @Test
  fun `ToolExecutionResultMessage round-trip`() {
    val message = ToolExecutionResultMessage.toolExecutionResultMessage("hello")
    val json = ChatMessageJson.serialize(message)
    val result = ChatMessageJson.deserialize<ToolExecutionResultMessage>(json)
    expectThat(result).isEqualTo(message)
  }
}
