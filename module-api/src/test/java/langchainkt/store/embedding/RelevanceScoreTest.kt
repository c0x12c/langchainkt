package langchainkt.store.embedding

import langchainkt.store.embedding.RelevanceScore.fromCosineSimilarity
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class RelevanceScoreTest {

  @Test
  fun should_convert_cosine_similarity_into_relevance_score() {
    assertThat(fromCosineSimilarity(-1.0)).isEqualTo(0.0)
    assertThat(fromCosineSimilarity(0.0)).isEqualTo(0.5)
    assertThat(fromCosineSimilarity(1.0)).isEqualTo(1.0)
  }
}
