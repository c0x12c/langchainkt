package langchainkt.model.huggingface

import dev.ai4j.openai4j.chat.ChatCompletionResponse
import dev.ai4j.openai4j.chat.Function
import dev.ai4j.openai4j.chat.FunctionCall
import dev.ai4j.openai4j.chat.Message
import dev.ai4j.openai4j.chat.Parameters
import dev.ai4j.openai4j.chat.Role
import dev.ai4j.openai4j.shared.Usage
import java.time.Duration
import langchainkt.agent.tool.ToolExecutionRequest.Companion.builder
import langchainkt.agent.tool.ToolParameters
import langchainkt.agent.tool.ToolSpecification
import langchainkt.data.message.AiMessage
import langchainkt.data.message.AiMessage.Companion.aiMessage
import langchainkt.data.message.ChatMessage
import langchainkt.data.message.SystemMessage
import langchainkt.data.message.ToolExecutionResultMessage
import langchainkt.data.message.UserMessage
import langchainkt.model.output.FinishReason
import langchainkt.model.output.TokenUsage

object InternalOpenAiHelper {
  const val OPENAI_URL = "https://api.openai.com/v1"
  const val OPENAI_DEMO_API_KEY = "demo"
  const val OPENAI_DEMO_URL = "http://langchain4j.dev/demo/openai/v1"

  fun defaultTimeoutFor(modelName: String): Duration {
    if (modelName.startsWith(OpenAiModelName.GPT_3_5_TURBO)) {
      return Duration.ofSeconds(7)
    } else if (modelName.startsWith(OpenAiModelName.GPT_4)) {
      return Duration.ofSeconds(20)
    }
    return Duration.ofSeconds(10)
  }

  fun toOpenAiMessages(messages: List<ChatMessage>): List<Message> {
    return messages.map { toOpenAiMessage(it) }
  }

  private fun toOpenAiMessage(message: ChatMessage): Message {
    return Message.builder()
      .role(roleFrom(message))
      .name(nameFrom(message))
      .content(message.text())
      .functionCall(functionCallFrom(message))
      .build()
  }

  private fun nameFrom(message: ChatMessage): String? {
    if (message is UserMessage) {
      return message.name()
    }
    return if (message is ToolExecutionResultMessage) {
      message.toolName
    } else {
      null
    }
  }

  private fun functionCallFrom(message: ChatMessage): FunctionCall? {
    if (message is AiMessage) {
      val toolExecutionRequest = message.toolExecutionRequest()
      if (toolExecutionRequest != null) {
        return FunctionCall.builder()
          .name(toolExecutionRequest.name())
          .arguments(toolExecutionRequest.arguments())
          .build()
      }
    }
    return null
  }

  fun roleFrom(message: ChatMessage): Role {
    return when (message) {
      is AiMessage -> Role.ASSISTANT
      is ToolExecutionResultMessage -> Role.FUNCTION
      is SystemMessage -> Role.SYSTEM
      else -> Role.USER
    }
  }

  fun toFunctions(toolSpecifications: Collection<ToolSpecification>): List<Function> {
    return toolSpecifications.map { toFunction(it) }
  }

  private fun toFunction(toolSpecification: ToolSpecification): Function {
    return Function.builder()
      .name(toolSpecification.name())
      .description(toolSpecification.description())
      .parameters(toOpenAiParameters(toolSpecification.parameters()))
      .build()
  }

  private fun toOpenAiParameters(toolParameters: ToolParameters?): Parameters {
    return if (toolParameters == null) {
      Parameters.builder().build()
    } else Parameters.builder()
      .properties(toolParameters.properties())
      .required(toolParameters.required())
      .build()
  }

  fun aiMessageFrom(response: ChatCompletionResponse): AiMessage {
    return if (response.content() != null) {
      aiMessage(response.content())
    } else {
      val functionCall = response.choices()[0].message().functionCall()
      val toolExecutionRequest = builder()
        .name(functionCall.name())
        .arguments(functionCall.arguments())
        .build()
      aiMessage(toolExecutionRequest)
    }
  }

  @JvmStatic
  fun tokenUsageFrom(openAiUsage: Usage?): TokenUsage? {
    return if (openAiUsage == null) {
      null
    } else TokenUsage(
      openAiUsage.promptTokens(),
      openAiUsage.completionTokens(),
      openAiUsage.totalTokens()
    )
  }

  @JvmStatic
  fun finishReasonFrom(openAiFinishReason: String?): FinishReason? {
    return when (openAiFinishReason) {
      "stop" -> FinishReason.STOP
      "length" -> FinishReason.LENGTH
      "function_call" -> FinishReason.TOOL_EXECUTION
      "content_filter" -> FinishReason.CONTENT_FILTER
      else -> null
    }
  }
}
