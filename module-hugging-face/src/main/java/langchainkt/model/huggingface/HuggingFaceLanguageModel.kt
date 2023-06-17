package langchainkt.model.huggingface

import java.time.Duration
import langchainkt.model.huggingface.HuggingFaceModelName.TII_UAE_FALCON_7B_INSTRUCT
import langchainkt.model.language.LanguageModel
import langchainkt.model.output.Response

class HuggingFaceLanguageModel(
  builder: Builder
) : LanguageModel {

  private val client: HuggingFaceClient = HuggingFaceClient(builder.accessToken, builder.modelId, builder.timeout)
  private val temperature: Double? = builder.temperature
  private val maxNewTokens: Int? = builder.maxNewTokens
  private val returnFullText: Boolean = builder.returnFullText
  private val waitForModel: Boolean = builder.waitForModel

  override fun generate(prompt: String): Response<String> {
    val request = TextGenerationRequest(
      inputs = prompt,
      parameters = Parameters(
        temperature = temperature,
        maxNewTokens = maxNewTokens,
        returnFullText = returnFullText
      ),
      options = Options(waitForModel = waitForModel)
    )
    val response = client.generate(request)
    return Response.from(response.generatedText)
  }

  class Builder(
    val accessToken: String
  ) {
    var modelId: String = TII_UAE_FALCON_7B_INSTRUCT
    var timeout = Duration.ofSeconds(15)
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

    fun build(): HuggingFaceLanguageModel {
      return HuggingFaceLanguageModel(this)
    }
  }

  companion object {
    fun builder(accessToken: String): Builder {
      return Builder(accessToken)
    }

    fun withAccessToken(accessToken: String): HuggingFaceLanguageModel {
      return builder(accessToken).build()
    }
  }
}
