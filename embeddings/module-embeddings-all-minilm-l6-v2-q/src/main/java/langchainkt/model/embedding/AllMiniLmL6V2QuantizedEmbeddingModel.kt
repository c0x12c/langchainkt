package langchainkt.model.embedding

/**
 * Quantized SentenceTransformers all-MiniLM-L6-v2 embedding model that runs within your Java application's process.
 *
 *
 * Maximum length of text (in tokens) that can be embedded at once: unlimited.
 * However, while you can embed very long texts, the quality of the embedding degrades as the text lengthens.
 * It is recommended to embed segments of no more than 256 tokens.
 *
 *
 * Embedding dimensions: 384
 *
 *
 * More details
 * [here](https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2) and
 * [here](https://www.sbert.net/docs/pretrained_models.html)
 */
class AllMiniLmL6V2QuantizedEmbeddingModel : AbstractInProcessEmbeddingModel() {

  override fun model(): OnnxBertBiEncoder {
    return MODEL
  }

  companion object {
    private val MODEL: OnnxBertBiEncoder = loadFromJar(
      "all-minilm-l6-v2-q.onnx",
      "bert-vocabulary-en.txt",
      PoolingMode.MEAN
    )
  }
}
