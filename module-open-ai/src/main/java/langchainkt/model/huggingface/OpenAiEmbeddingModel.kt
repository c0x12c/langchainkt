package langchainkt.model.huggingface

import dev.ai4j.openai4j.OpenAiClient
import dev.ai4j.openai4j.embedding.EmbeddingRequest
import java.net.Proxy
import java.time.Duration
import langchainkt.data.embedding.Embedding
import langchainkt.data.segment.TextSegment
import langchainkt.internal.RetryUtils
import langchainkt.model.Tokenizer
import langchainkt.model.embedding.EmbeddingModel
import langchainkt.model.embedding.TokenCountEstimator
import langchainkt.model.huggingface.InternalOpenAiHelper.OPENAI_DEMO_API_KEY
import langchainkt.model.huggingface.InternalOpenAiHelper.OPENAI_DEMO_URL
import langchainkt.model.huggingface.InternalOpenAiHelper.OPENAI_URL
import langchainkt.model.huggingface.InternalOpenAiHelper.tokenUsageFrom
import langchainkt.model.huggingface.OpenAiModelName.TEXT_EMBEDDING_ADA_002
import langchainkt.model.output.Response

/**
 * Represents an OpenAI embedding model, such as text-embedding-ada-002.
 */
class OpenAiEmbeddingModel(
  apiKey: String? = null,
  baseUrl: String? = null,
  modelName: String? = null,
  timeout: Duration? = null,
  maxRetries: Int? = null,
  proxy: Proxy? = null,
  logRequests: Boolean? = null,
  logResponses: Boolean? = null
) : EmbeddingModel, TokenCountEstimator {

  private val client: OpenAiClient
  private val modelName: String?
  private val maxRetries: Int
  private val tokenizer: Tokenizer

  init {
    var baseUrl = baseUrl
    var modelName = modelName
    var timeout = timeout
    var maxRetries = maxRetries
    baseUrl = baseUrl ?: OPENAI_URL
    if (OPENAI_DEMO_API_KEY == apiKey) {
      baseUrl = OPENAI_DEMO_URL
    }
    modelName = modelName ?: TEXT_EMBEDDING_ADA_002
    timeout = timeout ?: Duration.ofSeconds(15)
    maxRetries = maxRetries ?: 3
    client = OpenAiClient.builder()
      .openAiApiKey(apiKey)
      .baseUrl(baseUrl)
      .callTimeout(timeout)
      .connectTimeout(timeout)
      .readTimeout(timeout)
      .writeTimeout(timeout)
      .proxy(proxy)
      .logRequests(logRequests)
      .logResponses(logResponses)
      .build()
    this.modelName = modelName
    this.maxRetries = maxRetries
    tokenizer = OpenAiTokenizer(this.modelName)
  }

  override fun embedAll(textSegments: List<TextSegment>): Response<List<Embedding>> {
    val texts = textSegments
      .map { it.text() }
    return embedTexts(texts)
  }

  private fun embedTexts(texts: List<String>): Response<List<Embedding>> {
    val request = EmbeddingRequest.builder()
      .input(texts)
      .model(modelName)
      .build()
    val response = RetryUtils.withRetry({ client.embedding(request).execute() }, maxRetries)
    val embeddings = response
      .data()
      .map { Embedding.from(it.embedding()) }
    return Response.from(
      embeddings,
      tokenUsageFrom(response.usage())
    )
  }

  override fun estimateTokenCount(text: String): Int {
    return tokenizer.estimateTokenCountInText(text)
  }

  companion object {
    fun withApiKey(apiKey: String?): OpenAiEmbeddingModel {
      return OpenAiEmbeddingModel(apiKey)
    }
  }
}
