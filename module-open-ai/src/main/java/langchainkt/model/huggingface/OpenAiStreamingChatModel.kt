package langchainkt.model.huggingface

import dev.ai4j.openai4j.OpenAiClient
import dev.ai4j.openai4j.chat.ChatCompletionRequest
import dev.ai4j.openai4j.chat.ChatCompletionResponse
import dev.ai4j.openai4j.chat.Delta
import java.net.Proxy
import java.time.Duration
import langchainkt.agent.tool.ToolSpecification
import langchainkt.data.message.AiMessage
import langchainkt.data.message.ChatMessage
import langchainkt.model.StreamingResponseHandler
import langchainkt.model.Tokenizer
import langchainkt.model.chat.StreamingChatLanguageModel
import langchainkt.model.chat.TokenCountEstimator
import langchainkt.model.huggingface.InternalOpenAiHelper.OPENAI_URL
import langchainkt.model.huggingface.InternalOpenAiHelper.toFunctions
import langchainkt.model.huggingface.InternalOpenAiHelper.toOpenAiMessages
import langchainkt.model.huggingface.OpenAiModelName.GPT_3_5_TURBO

/**
 * Represents an OpenAI language model with a chat completion interface, such as gpt-3.5-turbo and gpt-4.
 * The model's response is streamed token by token and should be handled with [StreamingResponseHandler].
 */
class OpenAiStreamingChatModel(
  baseUrl: String? = null,
  apiKey: String? = null,
  modelName: String? = null,
  temperature: Double? = null,
  topP: Double = 0.0,
  stop: List<String> = emptyList(),
  maxTokens: Int = 0,
  presencePenalty: Double = 0.0,
  frequencyPenalty: Double = 0.0,
  timeout: Duration? = null,
  proxy: Proxy? = null,
  logRequests: Boolean? = null,
  logResponses: Boolean? = null,
) : StreamingChatLanguageModel, TokenCountEstimator {
  private val client: OpenAiClient
  private val modelName: String
  private val temperature: Double
  private val topP: Double
  private val stop: List<String>
  private val maxTokens: Int
  private val presencePenalty: Double
  private val frequencyPenalty: Double
  private val tokenizer: Tokenizer

  init {
    var timeout = timeout
    timeout = timeout ?: Duration.ofSeconds(5)
    client = OpenAiClient.builder()
      .baseUrl(baseUrl ?: OPENAI_URL)
      .openAiApiKey(apiKey)
      .callTimeout(timeout)
      .connectTimeout(timeout)
      .readTimeout(timeout)
      .writeTimeout(timeout)
      .proxy(proxy)
      .logRequests(logRequests)
      .logStreamingResponses(logResponses)
      .build()
    this.modelName = modelName ?: GPT_3_5_TURBO
    this.temperature = temperature ?: 0.7
    this.topP = topP
    this.stop = stop
    this.maxTokens = maxTokens
    this.presencePenalty = presencePenalty
    this.frequencyPenalty = frequencyPenalty
    tokenizer = OpenAiTokenizer(this.modelName)
  }

  override fun generate(messages: List<ChatMessage>, handler: StreamingResponseHandler<AiMessage>) {
    generate(messages, null, null, handler)
  }

  override fun generate(messages: List<ChatMessage>, toolSpecifications: List<ToolSpecification>, handler: StreamingResponseHandler<AiMessage>) {
    generate(messages, toolSpecifications, null, handler)
  }

  override fun generate(messages: List<ChatMessage>, toolSpecification: ToolSpecification, handler: StreamingResponseHandler<AiMessage>) {
    generate(messages, listOf(toolSpecification), toolSpecification, handler)
  }

  private fun generate(
    messages: List<ChatMessage>,
    toolSpecifications: List<ToolSpecification>?,
    toolThatMustBeExecuted: ToolSpecification?,
    handler: StreamingResponseHandler<AiMessage>
  ) {
    val requestBuilder = ChatCompletionRequest.builder()
      .stream(true)
      .model(modelName)
      .messages(toOpenAiMessages(messages))
      .temperature(temperature)
      .topP(topP)
      .stop(stop)
      .maxTokens(maxTokens)
      .presencePenalty(presencePenalty)
      .frequencyPenalty(frequencyPenalty)
    var inputTokenCount = tokenizer.estimateTokenCountInMessages(messages)
    if (!toolSpecifications.isNullOrEmpty()) {
      requestBuilder.functions(toFunctions(toolSpecifications))
      inputTokenCount += tokenizer.estimateTokenCountInToolSpecifications(toolSpecifications)
    }
    if (toolThatMustBeExecuted != null) {
      requestBuilder.functionCall(toolThatMustBeExecuted.name())
      inputTokenCount += tokenizer.estimateTokenCountInToolSpecification(toolThatMustBeExecuted)
    }
    val request: ChatCompletionRequest = requestBuilder.build()
    val responseBuilder = OpenAiStreamingResponseBuilder(inputTokenCount)
    client.chatCompletion(request)
      .onPartialResponse { response ->
        responseBuilder.append(response)
        handle(response, handler)
      }
      .onComplete {
        responseBuilder.build()?.let {
          handler.onComplete(it)
        }
      }
      .onError { error: Throwable? ->
        handler.onError(error)
      }
      .execute()
  }

  override fun estimateTokenCount(messages: List<ChatMessage>): Int {
    return tokenizer.estimateTokenCountInMessages(messages)
  }

  companion object {
    private fun handle(
      partialResponse: ChatCompletionResponse,
      handler: StreamingResponseHandler<AiMessage>
    ) {
      val choices = partialResponse.choices()
      if (choices == null || choices.isEmpty()) {
        return
      }
      val delta: Delta = choices[0].delta()
      val content = delta.content()
      if (content != null) {
        handler.onNext(content)
      }
    }

    fun withApiKey(apiKey: String?): OpenAiStreamingChatModel {
      return OpenAiStreamingChatModel(apiKey = apiKey)
    }
  }
}
