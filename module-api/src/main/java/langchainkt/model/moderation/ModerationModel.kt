package langchainkt.model.moderation

import langchainkt.data.message.ChatMessage
import langchainkt.data.segment.TextSegment
import langchainkt.model.input.Prompt
import langchainkt.model.output.Response

interface ModerationModel {
  fun moderate(text: String): Response<Moderation>
  fun moderate(prompt: Prompt): Response<Moderation>
  fun moderate(message: ChatMessage): Response<Moderation>
  fun moderate(messages: List<ChatMessage>): Response<Moderation>
  fun moderate(segment: TextSegment): Response<Moderation>
}
