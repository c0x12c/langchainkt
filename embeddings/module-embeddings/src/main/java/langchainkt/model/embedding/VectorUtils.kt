package langchainkt.model.embedding

import kotlin.math.sqrt
import langchainkt.data.embedding.Embedding

object VectorUtils {

  fun magnitudeOf(embedding: Embedding): Float {
    return magnitudeOf(embedding.vector())
  }

  fun magnitudeOf(vector: FloatArray): Float {
    var sumOfSquares = 0.0f
    for (v in vector) {
      sumOfSquares += v * v
    }
    return sqrt(sumOfSquares.toDouble()).toFloat()
  }
}
