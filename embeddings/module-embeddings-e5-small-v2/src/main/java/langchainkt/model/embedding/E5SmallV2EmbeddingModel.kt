package langchainkt.model.embedding

/**
 * Microsoft E5-small-v2 embedding model that runs within your Java application's process.
 *
 *
 * Maximum length of text (in tokens) that can be embedded at once: unlimited.
 * However, while you can embed very long texts, the quality of the embedding degrades as the text lengthens.
 * It is recommended to embed segments of no more than 512 tokens long.
 *
 *
 * Embedding dimensions: 384
 *
 *
 * It is recommended to use the "query:" prefix for queries and the "passage:" prefix for segments.
 *
 *
 * More details [here](https://huggingface.co/intfloat/e5-small-v2)
 */
class E5SmallV2EmbeddingModel : AbstractInProcessEmbeddingModel() {
  override fun model(): OnnxBertBiEncoder {
    return MODEL
  }

  companion object {
    private val MODEL = loadFromJar(
      "e5-small-v2.onnx",
      "bert-vocabulary-en.txt",
      PoolingMode.MEAN
    )
  }
}
