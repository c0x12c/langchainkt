package langchainkt.model.huggingface

import dev.ai4j.openai4j.OpenAiClient
import dev.ai4j.openai4j.completion.CompletionRequest
import java.net.Proxy
import java.time.Duration
import langchainkt.internal.RetryUtils
import langchainkt.model.Tokenizer
import langchainkt.model.language.LanguageModel
import langchainkt.model.language.TokenCountEstimator
import langchainkt.model.huggingface.InternalOpenAiHelper.OPENAI_URL
import langchainkt.model.huggingface.InternalOpenAiHelper.finishReasonFrom
import langchainkt.model.huggingface.InternalOpenAiHelper.tokenUsageFrom
import langchainkt.model.output.Response

/**
 * Represents an OpenAI language model with a completion interface, such as text-davinci-003.
 * However, it's recommended to use [OpenAiChatModel] instead,
 * as it offers more advanced features like function calling, multi-turn conversations, etc.
 */
class OpenAiLanguageModel(
  baseUrl: String? = null,
  apiKey: String? = null,
  modelName: String? = null,
  temperature: Double? = null,
  timeout: Duration? = null,
  maxRetries: Int? = null,
  proxy: Proxy? = null,
  logRequests: Boolean? = null,
  logResponses: Boolean? = null
) : LanguageModel, TokenCountEstimator {

  private val client: OpenAiClient
  private val modelName: String
  private val temperature: Double
  private val maxRetries: Int
  private val tokenizer: Tokenizer

  init {
    var baseUrl = baseUrl
    var modelName = modelName
    var temperature = temperature
    var timeout = timeout
    var maxRetries = maxRetries
    baseUrl = baseUrl ?: OPENAI_URL
    modelName = modelName ?: OpenAiModelName.TEXT_DAVINCI_003
    temperature = temperature ?: 0.7
    timeout = timeout ?: Duration.ofSeconds(15)
    maxRetries = maxRetries ?: 3
    client = OpenAiClient.builder()
      .baseUrl(baseUrl)
      .openAiApiKey(apiKey)
      .callTimeout(timeout)
      .connectTimeout(timeout)
      .readTimeout(timeout)
      .writeTimeout(timeout)
      .proxy(proxy)
      .logRequests(logRequests)
      .logResponses(logResponses)
      .build()
    this.modelName = modelName
    this.temperature = temperature
    this.maxRetries = maxRetries
    tokenizer = OpenAiTokenizer(this.modelName)
  }

  override fun generate(prompt: String): Response<String> {
    val request = CompletionRequest.builder()
      .model(modelName)
      .prompt(prompt)
      .temperature(temperature)
      .build()
    val response = RetryUtils.withRetry({ client.completion(request).execute() }, maxRetries)
    val completionChoice = response.choices()[0]
    return Response.from(
      completionChoice.text(),
      tokenUsageFrom(response.usage()),
      finishReasonFrom(completionChoice.finishReason())
    )
  }

  override fun estimateTokenCount(text: String): Int {
    return tokenizer.estimateTokenCountInText(text)
  }

  companion object {
    fun withApiKey(apiKey: String?): OpenAiLanguageModel {
      return OpenAiLanguageModel(apiKey = apiKey)
    }
  }
}
