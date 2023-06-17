package langchainkt.model.chat

import langchainkt.data.message.ChatMessage
import langchainkt.data.message.UserMessage
import langchainkt.data.segment.TextSegment
import langchainkt.model.input.Prompt

/**
 * Represents an interface for estimating the count of tokens in various text types such as a text, message, prompt, text segment, etc.
 * This can be useful when it's necessary to know in advance the cost of processing a specified text by the LLM.
 */
interface TokenCountEstimator {

  fun estimateTokenCount(text: String): Int {
    return estimateTokenCount(UserMessage.userMessage(text))
  }

  fun estimateTokenCount(userMessage: UserMessage): Int {
    return estimateTokenCount(listOf<ChatMessage>(userMessage))
  }

  fun estimateTokenCount(prompt: Prompt): Int {
    return estimateTokenCount(prompt.text())
  }

  fun estimateTokenCount(textSegment: TextSegment): Int {
    return estimateTokenCount(textSegment.text())
  }

  fun estimateTokenCount(messages: List<ChatMessage>): Int
}
