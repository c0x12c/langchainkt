package langchainkt.chain

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import langchainkt.data.message.AiMessage.Companion.aiMessage
import langchainkt.data.message.ChatMessage
import langchainkt.data.message.UserMessage.Companion.userMessage
import langchainkt.memory.ChatMemory
import langchainkt.memory.chat.MessageWindowChatMemory
import langchainkt.model.chat.ChatLanguageModel
import langchainkt.model.output.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConversationalChainTest {

  private val model = mockk<ChatLanguageModel>()

  @Test
  fun should_store_user_and_ai_messages_in_chat_memory() {
    // Given
    val aiMessage = "Hi there"
    every {
      model.generate(any<List<ChatMessage>>())
    } returns Response.from(aiMessage(aiMessage))
    val chatMemory: ChatMemory = spyk(MessageWindowChatMemory.withMaxMessages(10))
    val chain = ConversationalChain(model, chatMemory)
    val userMessage = "Hello"

    // When
    val response = chain.execute(userMessage)

    // Then
    assertThat(response).isEqualTo(aiMessage)
    verify {
      chatMemory.add(userMessage(userMessage))
    }
    verify(exactly = 3) {
      chatMemory.messages()
    }
    verify {
      model.generate(listOf(userMessage(userMessage)))
    }
    verify {
      chatMemory.add(aiMessage(aiMessage))
    }
  }
}
