package langchainkt.model.embedding

/**
 * Quantized BAAI bge-small-en embedding model that runs within your Java application's process.
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
 * It is recommended to add "Represent this sentence for searching relevant passages:" prefix to a query.
 *
 *
 * More details [here](https://huggingface.co/BAAI/bge-small-en)
 */
class BgeSmallEnQuantizedEmbeddingModel : AbstractInProcessEmbeddingModel() {
  override fun model(): OnnxBertBiEncoder {
    return MODEL
  }

  companion object {
    private val MODEL: OnnxBertBiEncoder = loadFromJar(
      "bge-small-en-q.onnx",
      "bert-vocabulary-en.txt",
      PoolingMode.CLS
    )
  }
}
