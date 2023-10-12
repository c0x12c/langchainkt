package langchainkt.model.embedding

/**
 * BAAI bge-small-zh embedding model that runs within your Java application's process.
 *
 *
 * Maximum length of text (in tokens) that can be embedded at once: unlimited.
 * However, while you can embed very long texts, the quality of the embedding degrades as the text lengthens.
 * It is recommended to embed segments of no more than 512 tokens long.
 *
 *
 * Embedding dimensions: 512
 *
 *
 * It is recommended to add "为这个句子生成表示以用于检索相关文章：" prefix to a query.
 *
 *
 * More details [here](https://huggingface.co/BAAI/bge-small-zh)
 */
class BgeSmallZhEmbeddingModel : AbstractInProcessEmbeddingModel() {

  override fun model(): OnnxBertBiEncoder {
    return MODEL
  }

  companion object {
    private val MODEL = loadFromJar(
      "bge-small-zh.onnx",
      "bge-small-zh-vocabulary.txt",
      PoolingMode.CLS
    )
  }
}
