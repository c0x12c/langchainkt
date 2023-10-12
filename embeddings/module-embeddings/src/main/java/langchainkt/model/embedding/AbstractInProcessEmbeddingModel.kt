package langchainkt.model.embedding

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors
import langchainkt.data.embedding.Embedding
import langchainkt.data.embedding.Embedding.Companion.from
import langchainkt.data.segment.TextSegment
import langchainkt.model.output.Response

abstract class AbstractInProcessEmbeddingModel : EmbeddingModel, TokenCountEstimator {

  protected abstract fun model(): OnnxBertBiEncoder

  override fun embedAll(textSegments: List<TextSegment>): Response<List<Embedding>> {
    val embeddings = textSegments.stream()
      .map { segment: TextSegment -> from(model().embed(segment.text())) }
      .collect(Collectors.toList())
    return Response.from(embeddings)
  }

  override fun estimateTokenCount(text: String): Int {
    return model().countTokens(text)
  }

  companion object {

    fun loadFromJar(modelFileName: String, vocabularyFileName: String, poolingMode: PoolingMode): OnnxBertBiEncoder {
      val inputStream = AbstractInProcessEmbeddingModel::class.java.getResourceAsStream("/$modelFileName")!!
      return OnnxBertBiEncoder(
        inputStream,
        AbstractInProcessEmbeddingModel::class.java.getResource("/$vocabularyFileName")!!,
        poolingMode
      )
    }

    fun loadFromFileSystem(pathToModel: Path): OnnxBertBiEncoder {
      return try {
        OnnxBertBiEncoder(
          Files.newInputStream(pathToModel),
          AbstractInProcessEmbeddingModel::class.java.getResource("/bert-vocabulary-en.txt")!!,
          PoolingMode.MEAN
        )
      } catch (e: IOException) {
        throw RuntimeException(e)
      }
    }
  }
}
