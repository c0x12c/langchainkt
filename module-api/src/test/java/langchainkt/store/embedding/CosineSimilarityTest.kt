package langchainkt.store.embedding

import langchainkt.data.embedding.Embedding.Companion.from
import langchainkt.store.embedding.CosineSimilarity.between
import langchainkt.store.embedding.CosineSimilarity.fromRelevanceScore
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.junit.jupiter.api.Test

class CosineSimilarityTest {

  @Test
  fun should_calculate_cosine_similarity() {
    val embeddingA = from(floatArrayOf(1f, 1f, 1f))
    val embeddingB = from(floatArrayOf(-1f, -1f, -1f))
    assertThat(between(embeddingA, embeddingA)).isCloseTo(1.0, Percentage.withPercentage(1.0))
    assertThat(between(embeddingA, embeddingB)).isCloseTo(-1.0, Percentage.withPercentage(1.0))
  }

  @Test
  fun should_convert_relevance_score_into_cosine_similarity() {
    assertThat(fromRelevanceScore(0.0)).isEqualTo(-1.0)
    assertThat(fromRelevanceScore(0.5)).isEqualTo(0.0)
    assertThat(fromRelevanceScore(1.0)).isEqualTo(1.0)
  }
}
