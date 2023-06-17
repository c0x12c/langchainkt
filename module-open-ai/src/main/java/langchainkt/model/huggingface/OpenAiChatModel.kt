package langchainkt.model.huggingface

import dev.ai4j.openai4j.OpenAiClient
import dev.ai4j.openai4j.chat.ChatCompletionRequest
import java.net.Proxy
import java.time.Duration
import langchainkt.agent.tool.ToolSpecification
import langchainkt.data.message.AiMessage
import langchainkt.data.message.ChatMessage
import langchainkt.internal.RetryUtils
import langchainkt.model.Tokenizer
import langchainkt.model.chat.ChatLanguageModel
import langchainkt.model.chat.TokenCountEstimator
import langchainkt.model.output.Response

/**
 * Represents an OpenAI language model with a chat completion interface, such as gpt-3.5-turbo and gpt-4.
 */
class OpenAiChatModel private constructor(
  builder: Builder
) : ChatLanguageModel, TokenCountEstimator {

  private val client: OpenAiClient
  private val tokenizer: Tokenizer
  private val modelName: String = builder.modelName ?: OpenAiModelName.GPT_3_5_TURBO
  private val temperature: Double
  private val topP: Double
  private val stop: List<String>
  private val maxTokens: Int
  private val presencePenalty: Double
  private val frequencyPenalty: Double
  private val maxRetries: Int

  init {
    this.temperature = builder.temperature ?: 0.7
    this.topP = builder.topP
    this.stop = builder.stop
    this.maxTokens = builder.maxTokens
    this.presencePenalty = builder.presencePenalty
    this.frequencyPenalty = builder.frequencyPenalty
    this.maxRetries = builder.maxRetries ?: 3

    var baseUrl = builder.baseUrl
    var timeout = builder.timeout
    baseUrl = baseUrl ?: InternalOpenAiHelper.OPENAI_URL
    if (InternalOpenAiHelper.OPENAI_DEMO_API_KEY == builder.apiKey) {
      baseUrl = InternalOpenAiHelper.OPENAI_DEMO_URL
    }
    timeout = timeout ?: InternalOpenAiHelper.defaultTimeoutFor(modelName)
    tokenizer = OpenAiTokenizer(modelName)
    client = OpenAiClient.builder()
      .openAiApiKey(builder.apiKey)
      .baseUrl(baseUrl)
      .callTimeout(timeout)
      .connectTimeout(timeout)
      .readTimeout(timeout)
      .writeTimeout(timeout)
      .proxy(builder.proxy)
      .logRequests(builder.logRequests)
      .logResponses(builder.logResponses)
      .build()
  }

  internal class Builder internal constructor(
    internal val apiKey: String
  ) {
    internal var baseUrl: String? = null
    internal var modelName: String? = null
    internal var temperature: Double? = null
    internal var topP: Double = 0.0
    internal var stop: List<String> = emptyList()
    internal var maxTokens: Int = 0
    internal var presencePenalty: Double = 0.0
    internal var frequencyPenalty: Double = 0.0
    internal var timeout: Duration? = null
    internal var maxRetries: Int? = null
    internal var proxy: Proxy? = null
    internal var logRequests: Boolean? = null
    internal var logResponses: Boolean? = null

    fun withBaseUrl(baseUrl: String) = apply { this.baseUrl = baseUrl }
    fun withModelName(modelName: String?) = apply { this.modelName = modelName }
    fun withTemperature(temperature: Double?) = apply { this.temperature = temperature }
    fun withTopP(topP: Double) = apply { this.topP = topP }
    fun withStop(stop: List<String>) = apply { this.stop = stop }
    fun withMaxTokens(maxTokens: Int) = apply { this.maxTokens = maxTokens }
    fun withPresencePenalty(presencePenalty: Double) = apply { this.presencePenalty = presencePenalty }
    fun withFrequencyPenalty(frequencyPenalty: Double) = apply { this.frequencyPenalty = frequencyPenalty }
    fun withTimeout(timeout: Duration) = apply { this.timeout = timeout }
    fun withMaxRetries(maxRetries: Int?) = apply { this.maxRetries = maxRetries }
    fun withProxy(proxy: Proxy) = apply { this.proxy = proxy }
    fun withLogRequests(logRequests: Boolean) = apply { this.logRequests = logRequests }
    fun withLogResponses(logResponses: Boolean) = apply { this.logResponses = logResponses }

    fun build() = OpenAiChatModel(this)
  }

  // Rest of your OpenAiChatModel class implementation...

  override fun generate(messages: List<ChatMessage>): Response<AiMessage> {
    return generate(messages, null, null)
  }

  override fun generate(messages: List<ChatMessage>, toolSpecifications: List<ToolSpecification>): Response<AiMessage> {
    return generate(messages, toolSpecifications, null)
  }

  override fun generate(messages: List<ChatMessage>, toolSpecification: ToolSpecification): Response<AiMessage> {
    return generate(messages, listOf(toolSpecification), toolSpecification)
  }

  private fun generate(messages: List<ChatMessage>,
                       toolSpecifications: List<ToolSpecification>?,
                       toolThatMustBeExecuted: ToolSpecification?
  ): Response<AiMessage> {
    val requestBuilder: ChatCompletionRequest.Builder = ChatCompletionRequest.builder()
      .model(modelName)
      .messages(InternalOpenAiHelper.toOpenAiMessages(messages))
      .temperature(temperature)
      .topP(topP)
      .stop(stop)
      .maxTokens(maxTokens)
      .presencePenalty(presencePenalty)
      .frequencyPenalty(frequencyPenalty)
    if (!toolSpecifications.isNullOrEmpty()) {
      requestBuilder.functions(InternalOpenAiHelper.toFunctions(toolSpecifications))
    }
    if (toolThatMustBeExecuted != null) {
      requestBuilder.functionCall(toolThatMustBeExecuted.name())
    }
    val request = requestBuilder.build()
    val response = RetryUtils.withRetry({ client.chatCompletion(request).execute() }, maxRetries)
    return Response.from(
      InternalOpenAiHelper.aiMessageFrom(response),
      InternalOpenAiHelper.tokenUsageFrom(response.usage()),
      InternalOpenAiHelper.finishReasonFrom(response.choices()[0].finishReason())
    )
  }

  override fun estimateTokenCount(messages: List<ChatMessage>): Int {
    return tokenizer.estimateTokenCountInMessages(messages)
  }

  companion object {

    @JvmStatic
    fun withApiKey(apiKey: String): OpenAiChatModel {
      return Builder(apiKey).build()
    }
  }
}

