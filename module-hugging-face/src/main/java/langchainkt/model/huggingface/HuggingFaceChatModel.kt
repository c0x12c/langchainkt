package langchainkt.model.huggingface

import java.time.Duration
import langchainkt.agent.tool.ToolSpecification
import langchainkt.data.message.AiMessage
import langchainkt.data.message.ChatMessage
import langchainkt.internal.Utils.isNullOrBlank
import langchainkt.model.chat.ChatLanguageModel
import langchainkt.model.huggingface.HuggingFaceModelName.TII_UAE_FALCON_7B_INSTRUCT
import langchainkt.model.output.Response

class HuggingFaceChatModel private constructor(
  builder: Builder
) : ChatLanguageModel {

  private val client: HuggingFaceClient = HuggingFaceClient(builder.accessToken, builder.modelId, builder.timeout)
  private val temperature: Double? = builder.temperature
  private val maxNewTokens: Int? = builder.maxNewTokens
  private val returnFullText: Boolean = builder.returnFullText
  private val waitForModel: Boolean = builder.waitForModel

  override fun generate(messages: List<ChatMessage>): Response<AiMessage> {
    val request = TextGenerationRequest(
      inputs = messages.joinToString("\n") { it.text() },
      parameters = Parameters(
        temperature = temperature,
        maxNewTokens = maxNewTokens,
        returnFullText = returnFullText
      ),
      options = Options(waitForModel = waitForModel)
    )
    val response = client.chat(request)
    return Response.from(AiMessage.from(response.generatedText))
  }

  override fun generate(messages: List<ChatMessage>, toolSpecifications: List<ToolSpecification>): Response<AiMessage> {
    throw IllegalArgumentException("Tools are currently not supported for HuggingFace models")
  }

  override fun generate(messages: List<ChatMessage>, toolSpecification: ToolSpecification): Response<AiMessage> {
    throw IllegalArgumentException("Tools are currently not supported for HuggingFace models")
  }

  class Builder(
    val accessToken: String
  ) {
    var modelId: String = TII_UAE_FALCON_7B_INSTRUCT
    var timeout: Duration = Duration.ofSeconds(15)
    var temperature: Double? = null
    var maxNewTokens: Int? = null
    var returnFullText = false
    var waitForModel = true

    fun modelId(modelId: String?): Builder {
      if (modelId != null) {
        this.modelId = modelId
      }
      return this
    }

    fun timeout(timeout: Duration?): Builder {
      if (timeout != null) {
        this.timeout = timeout
      }
      return this
    }

    fun temperature(temperature: Double?): Builder {
      this.temperature = temperature
      return this
    }

    fun maxNewTokens(maxNewTokens: Int?): Builder {
      this.maxNewTokens = maxNewTokens
      return this
    }

    fun returnFullText(returnFullText: Boolean?): Builder {
      if (returnFullText != null) {
        this.returnFullText = returnFullText
      }
      return this
    }

    fun waitForModel(waitForModel: Boolean?): Builder {
      if (waitForModel != null) {
        this.waitForModel = waitForModel
      }
      return this
    }

    fun build(): HuggingFaceChatModel {
      require(!isNullOrBlank(accessToken)) { "HuggingFace access token must be defined. It can be generated here: https://huggingface.co/settings/tokens" }
      return HuggingFaceChatModel(this)
    }
  }

  companion object {
    fun builder(accessToken: String): Builder {
      return Builder(accessToken)
    }

    fun withAccessToken(accessToken: String): HuggingFaceChatModel {
      return builder(accessToken).build()
    }
  }
}
