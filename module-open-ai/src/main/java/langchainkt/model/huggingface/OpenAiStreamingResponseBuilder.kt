package langchainkt.model.huggingface

import dev.ai4j.openai4j.chat.ChatCompletionResponse
import dev.ai4j.openai4j.completion.CompletionResponse
import langchainkt.agent.tool.ToolExecutionRequest.Companion.builder
import langchainkt.data.message.AiMessage
import langchainkt.model.output.Response
import langchainkt.model.output.TokenUsage

class OpenAiStreamingResponseBuilder(
  private val inputTokenCount: Int
) {

  private val contentBuilder = StringBuilder()
  private val toolNameBuilder = StringBuilder()
  private val toolArgumentsBuilder = StringBuilder()
  private var outputTokenCount = 0
  private var finishReason: String? = null
  fun append(partialResponse: ChatCompletionResponse?) {
    if (partialResponse == null) {
      return
    }
    val choices = partialResponse.choices()
    if (choices == null || choices.isEmpty()) {
      return
    }
    val chatCompletionChoice = choices[0] ?: return
    val finishReason = chatCompletionChoice.finishReason()
    if (finishReason != null) {
      this.finishReason = finishReason
    }
    val delta = chatCompletionChoice.delta() ?: return
    val content = delta.content()
    if (content != null) {
      contentBuilder.append(content)
      outputTokenCount++
      return
    }
    val functionCall = delta.functionCall()
    if (functionCall != null) {
      if (functionCall.name() != null) {
        toolNameBuilder.append(functionCall.name())
        outputTokenCount++
      }
      if (functionCall.arguments() != null) {
        toolArgumentsBuilder.append(functionCall.arguments())
        outputTokenCount++
      }
    }
  }

  fun append(partialResponse: CompletionResponse?) {
    if (partialResponse == null) {
      return
    }
    val choices = partialResponse.choices()
    if (choices == null || choices.isEmpty()) {
      return
    }
    val completionChoice = choices[0] ?: return
    val finishReason = completionChoice.finishReason()
    if (finishReason != null) {
      this.finishReason = finishReason
    }
    val token = completionChoice.text()
    if (token != null) {
      contentBuilder.append(token)
      outputTokenCount++
    }
  }

  fun build(): Response<AiMessage>? {
    val content = contentBuilder.toString()
    if (!content.isEmpty()) {
      return Response.from(
        AiMessage.from(content),
        TokenUsage(inputTokenCount, outputTokenCount),
        InternalOpenAiHelper.finishReasonFrom(finishReason)
      )
    }
    val toolName = toolNameBuilder.toString()
    return if (!toolName.isEmpty()) {
      Response.from(
        AiMessage.from(builder()
          .name(toolName)
          .arguments(toolArgumentsBuilder.toString())
          .build()),
        TokenUsage(inputTokenCount, outputTokenCount),
        InternalOpenAiHelper.finishReasonFrom(finishReason)
      )
    } else {
      null
    }
  }
}
