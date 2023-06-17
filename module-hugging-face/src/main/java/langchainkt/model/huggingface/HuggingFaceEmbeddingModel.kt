package langchainkt.model.huggingface

import java.time.Duration
import langchainkt.data.embedding.Embedding
import langchainkt.data.segment.TextSegment
import langchainkt.model.embedding.EmbeddingModel
import langchainkt.model.huggingface.HuggingFaceModelName.SENTENCE_TRANSFORMERS_ALL_MINI_LM_L6_V2
import langchainkt.model.output.Response

class HuggingFaceEmbeddingModel(
  accessToken: String,
  modelId: String? = null,
  waitForModel: Boolean? = null,
  timeout: Duration? = null
) : EmbeddingModel {
  private val client: HuggingFaceClient
  private val waitForModel: Boolean

  init {
    client = HuggingFaceClient(
      accessToken,
      modelId ?: SENTENCE_TRANSFORMERS_ALL_MINI_LM_L6_V2,
      timeout ?: DEFAULT_TIMEOUT
    )
    this.waitForModel = waitForModel ?: true
  }

  override fun embedAll(textSegments: List<TextSegment>): Response<List<Embedding>> {
    val texts = textSegments.map { it.text() }
    return embedTexts(texts)
  }

  private fun embedTexts(texts: List<String>): Response<List<Embedding>> {
    val request = EmbeddingRequest(texts, waitForModel)
    val response = client.embed(request)
    val embeddings = response.map { Embedding.from(it) }
    return Response.from(embeddings)
  }

  companion object {
    private val DEFAULT_TIMEOUT = Duration.ofSeconds(15)

    fun withAccessToken(accessToken: String): HuggingFaceEmbeddingModel {
      return HuggingFaceEmbeddingModel(accessToken, null, null, null)
    }
  }
}
