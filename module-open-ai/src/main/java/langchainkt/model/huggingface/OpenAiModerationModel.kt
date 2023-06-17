package langchainkt.model.huggingface

import dev.ai4j.openai4j.OpenAiClient
import dev.ai4j.openai4j.moderation.ModerationRequest
import java.net.Proxy
import java.time.Duration
import langchainkt.data.message.ChatMessage
import langchainkt.data.segment.TextSegment
import langchainkt.internal.RetryUtils
import langchainkt.model.input.Prompt
import langchainkt.model.moderation.Moderation
import langchainkt.model.moderation.ModerationModel
import langchainkt.model.huggingface.InternalOpenAiHelper.OPENAI_DEMO_API_KEY
import langchainkt.model.huggingface.InternalOpenAiHelper.OPENAI_DEMO_URL
import langchainkt.model.huggingface.InternalOpenAiHelper.OPENAI_URL
import langchainkt.model.huggingface.OpenAiModelName.TEXT_MODERATION_LATEST
import langchainkt.model.output.Response

/**
 * Represents an OpenAI moderation model, such as text-moderation-latest.
 */
class OpenAiModerationModel(
  apiKey: String? = null,
  baseUrl: String? = null,
  modelName: String? = null,
  timeout: Duration? = null,
  maxRetries: Int? = null,
  proxy: Proxy? = null,
  logRequests: Boolean? = null,
  logResponses: Boolean? = null
) : ModerationModel {

  private val client: OpenAiClient
  private val modelName: String?
  private val maxRetries: Int

  init {
    var baseUrl = baseUrl
    var modelName = modelName
    var timeout = timeout
    var maxRetries = maxRetries
    baseUrl = baseUrl ?: OPENAI_URL
    if (OPENAI_DEMO_API_KEY == apiKey) {
      baseUrl = OPENAI_DEMO_URL
    }
    modelName = modelName ?: TEXT_MODERATION_LATEST
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
  }

  override fun moderate(text: String): Response<Moderation> {
    return moderateInternal(listOf(text))
  }

  private fun moderateInternal(inputs: List<String>): Response<Moderation> {
    val request = ModerationRequest.builder()
      .model(modelName)
      .input(inputs)
      .build()
    val response = RetryUtils.withRetry({ client.moderation(request).execute() }, maxRetries)
    for ((i, moderationResult) in response.results().withIndex()) {
      if (moderationResult.isFlagged) {
        return Response.from(Moderation.flagged(inputs[i]))
      }
    }
    return Response.from(Moderation.notFlagged())
  }

  override fun moderate(prompt: Prompt): Response<Moderation> {
    return moderate(prompt.text())
  }

  override fun moderate(message: ChatMessage): Response<Moderation> {
    return moderate(message.text())
  }

  override fun moderate(messages: List<ChatMessage>): Response<Moderation> {
    val inputs = messages.mapNotNull { it.text }
    return moderateInternal(inputs)
  }

  override fun moderate(segment: TextSegment): Response<Moderation> {
    return moderate(segment.text())
  }

  companion object {
    fun withApiKey(apiKey: String?): OpenAiModerationModel {
      return OpenAiModerationModel(apiKey = apiKey)
    }
  }
}
