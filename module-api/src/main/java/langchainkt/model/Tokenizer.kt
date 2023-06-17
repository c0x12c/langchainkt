package langchainkt.model

import java.util.function.Consumer
import langchainkt.agent.tool.ToolSpecification
import langchainkt.agent.tool.ToolSpecifications
import langchainkt.data.message.ChatMessage

interface Tokenizer {
  fun estimateTokenCountInText(text: String): Int
  fun estimateTokenCountInMessage(message: ChatMessage): Int
  fun estimateTokenCountInMessages(messages: Iterable<ChatMessage>): Int
  fun estimateTokenCountInToolSpecifications(toolSpecifications: Iterable<ToolSpecification>): Int

  fun estimateTokenCountInTools(objectsWithTools: Iterable<Any>): Int {
    val toolSpecifications: MutableList<ToolSpecification> = ArrayList()
    objectsWithTools.forEach(Consumer { toolSpecifications.addAll(ToolSpecifications.toolSpecificationsFrom(it)) })
    return estimateTokenCountInToolSpecifications(toolSpecifications)
  }

  fun estimateTokenCountInToolSpecification(toolSpecification: ToolSpecification): Int {
    return estimateTokenCountInToolSpecifications(listOf(toolSpecification))
  }

  fun estimateTokenCountInTools(objectWithTools: Any): Int {
    return estimateTokenCountInTools(listOf(objectWithTools))
  }

}
