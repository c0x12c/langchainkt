package langchainkt.store.embedding

import kotlin.math.sqrt
import langchainkt.data.embedding.Embedding
import langchainkt.internal.Exceptions

object CosineSimilarity {
  /**
   * Calculates cosine similarity between two vectors.
   *
   *
   * Cosine similarity measures the cosine of the angle between two vectors, indicating their directional similarity.
   * It produces a value in the range:
   *
   *
   * -1 indicates vectors are diametrically opposed (opposite directions).
   *
   *
   * 0 indicates vectors are orthogonal (no directional similarity).
   *
   *
   * 1 indicates vectors are pointing in the same direction (but not necessarily of the same magnitude).
   *
   *
   * Not to be confused with cosine distance ([0..2]), which quantifies how different two vectors are.
   *
   * @param a first embedding vector
   * @param b second embedding vector
   * @return cosine similarity in the range [-1..1]
   */
  @JvmStatic
  fun between(a: Embedding, b: Embedding): Double {
    val vectorA = a.vector()
    val vectorB = b.vector()
    if (vectorA.size != vectorB.size) {
      throw Exceptions.illegalArgument("Length of vector a (%s) must be equal to the length of vector b (%s)",
        vectorA.size, vectorB.size)
    }
    var dotProduct = 0.0
    var normA = 0.0
    var normB = 0.0
    for (i in vectorA.indices) {
      dotProduct += (vectorA[i] * vectorB[i]).toDouble()
      normA += (vectorA[i] * vectorA[i]).toDouble()
      normB += (vectorB[i] * vectorB[i]).toDouble()
    }
    return dotProduct / (sqrt(normA) * sqrt(normB))
  }

  /**
   * Converts relevance score into cosine similarity.
   *
   * @param relevanceScore Relevance score in the range [0..1] where 0 is not relevant and 1 is relevant.
   * @return Cosine similarity in the range [-1..1] where -1 is not relevant and 1 is relevant.
   */
  @JvmStatic
  fun fromRelevanceScore(relevanceScore: Double): Double {
    return relevanceScore * 2 - 1
  }
}
