package langchainkt.service

import langchainkt.agent.tool.ToolExecutor
import langchainkt.agent.tool.ToolSpecification
import langchainkt.data.segment.TextSegment
import langchainkt.memory.ChatMemory
import langchainkt.memory.chat.ChatMemoryProvider
import langchainkt.model.chat.ChatLanguageModel
import langchainkt.model.chat.StreamingChatLanguageModel
import langchainkt.model.moderation.ModerationModel
import langchainkt.retriever.Retriever

internal class AiServiceContext {
  var aiServiceClass: Class<*>? = null
  var chatModel: ChatLanguageModel? = null
  var chatMemories: MutableMap<Any, ChatMemory?>? = null
  var moderationModel: ModerationModel? = null
  var retriever: Retriever<TextSegment>? = null
  lateinit var streamingChatModel: StreamingChatLanguageModel
  lateinit var chatMemoryProvider: ChatMemoryProvider
  lateinit var toolSpecifications: MutableList<ToolSpecification>
  lateinit var toolExecutors: MutableMap<String, ToolExecutor>

  fun hasChatMemory(): Boolean {
    return chatMemories != null
  }

  fun chatMemory(memoryId: Any): ChatMemory? {
    return chatMemories?.computeIfAbsent(memoryId) {
      chatMemoryProvider[memoryId]
    }
  }
}
