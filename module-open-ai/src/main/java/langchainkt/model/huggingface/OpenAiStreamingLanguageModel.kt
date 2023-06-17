package langchainkt.model.huggingface

import dev.ai4j.openai4j.OpenAiClient
import dev.ai4j.openai4j.completion.CompletionRequest
import dev.ai4j.openai4j.completion.CompletionResponse
import java.net.Proxy
import java.time.Duration
import langchainkt.model.StreamingResponseHandler
import langchainkt.model.Tokenizer
import langchainkt.model.language.StreamingLanguageModel
import langchainkt.model.language.TokenCountEstimator
import langchainkt.model.huggingface.InternalOpenAiHelper.OPENAI_URL
import langchainkt.model.huggingface.OpenAiModelName.TEXT_DAVINCI_003
import langchainkt.model.output.Response

/**
 * Represents an OpenAI language model with a completion interface, such as text-davinci-003.
 * The model's response is streamed token by token and should be handled with [StreamingResponseHandler].
 * However, it's recommended to use [OpenAiStreamingChatModel] instead,
 * as it offers more advanced features like function calling, multi-turn conversations, etc.
 */
class OpenAiStreamingLanguageModel(
  apiKey: String? = null,
  baseUrl: String? = null,
  modelName: String? = null,
  temperature: Double? = null,
  timeout: Duration? = null,
  proxy: Proxy? = null,
  logRequests: Boolean? = null,
  logResponses: Boolean? = null
) : StreamingLanguageModel, TokenCountEstimator {

  private val client: OpenAiClient
  private val modelName: String?
  private val temperature: Double
  private val tokenizer: Tokenizer

  init {
    var baseUrl = baseUrl
    var modelName = modelName
    var temperature = temperature
    var timeout = timeout
    baseUrl = baseUrl ?: OPENAI_URL
    modelName = modelName ?: TEXT_DAVINCI_003
    temperature = temperature ?: 0.7
    timeout = timeout ?: Duration.ofSeconds(60)
    client = OpenAiClient.builder()
      .baseUrl(baseUrl)
      .openAiApiKey(apiKey)
      .callTimeout(timeout)
      .connectTimeout(timeout)
      .readTimeout(timeout)
      .writeTimeout(timeout)
      .proxy(proxy)
      .logRequests(logRequests)
      .logStreamingResponses(logResponses)
      .build()
    this.modelName = modelName
    this.temperature = temperature
    tokenizer = OpenAiTokenizer(this.modelName)
  }

  override fun generate(prompt: String, handler: StreamingResponseHandler<String>) {
    val request: CompletionRequest = CompletionRequest.builder()
      .model(modelName)
      .prompt(prompt)
      .temperature(temperature)
      .build()
    val inputTokenCount = tokenizer.estimateTokenCountInText(prompt)
    val responseBuilder = OpenAiStreamingResponseBuilder(inputTokenCount)
    client.completion(request)
      .onPartialResponse { response: CompletionResponse ->
        responseBuilder.append(response)
        val token = response.text()
        if (token != null) {
          handler.onNext(token)
        }
      }
      .onComplete {
        responseBuilder.build()?.let { response ->
          handler.onComplete(Response.from(
            response.content().text(),
            response.tokenUsage(),
            response.finishReason()
          ))
        }
      }
      .onError { error: Throwable? ->
        handler.onError(error)
      }
      .execute()
  }

  override fun estimateTokenCount(text: String): Int {
    return tokenizer.estimateTokenCountInText(text)
  }

  companion object {
    fun withApiKey(apiKey: String): OpenAiStreamingLanguageModel {
      return OpenAiStreamingLanguageModel(apiKey = apiKey)
    }
  }
}
