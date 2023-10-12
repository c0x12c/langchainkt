package langchainkt.model.embedding

import langchainkt.model.embedding.VectorUtils.magnitudeOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class BgeSmallZhQuantizedEmbeddingModelTest {

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed() {
    val model: EmbeddingModel = BgeSmallZhQuantizedEmbeddingModel()
    val first = model.embed("你好").content()
    assertThat(first.vector()).hasSize(512)
    val second = model.embed("您好").content()
    assertThat(second.vector()).hasSize(512)
    val cosineSimilarity = langchainkt.store.embedding.CosineSimilarity.between(first, second)
    assertThat(langchainkt.store.embedding.RelevanceScore.fromCosineSimilarity(cosineSimilarity)).isGreaterThan(0.97)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed_510_token_long_text() {
    val model: EmbeddingModel = BgeSmallZhQuantizedEmbeddingModel()
    val oneToken = "书 "
    val embedding: langchainkt.data.embedding.Embedding = model.embed(oneToken.repeat(510)).content()
    assertThat(embedding.vector()).hasSize(512)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed_text_longer_than_510_tokens_by_splitting_and_averaging_embeddings_of_splits() {
    val model: EmbeddingModel = BgeSmallZhQuantizedEmbeddingModel()
    val oneToken = "书 "
    val embedding510: langchainkt.data.embedding.Embedding = model.embed(oneToken.repeat(510)).content()
    assertThat(embedding510.vector()).hasSize(512)
    val embedding511: langchainkt.data.embedding.Embedding = model.embed(oneToken.repeat(511)).content()
    assertThat(embedding511.vector()).hasSize(512)
    val cosineSimilarity = langchainkt.store.embedding.CosineSimilarity.between(embedding510, embedding511)
    assertThat(langchainkt.store.embedding.RelevanceScore.fromCosineSimilarity(cosineSimilarity)).isGreaterThan(0.99)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_produce_normalized_vectors() {
    val model: EmbeddingModel = BgeSmallZhQuantizedEmbeddingModel()
    val oneToken = "书 "
    assertThat(magnitudeOf(model.embed(oneToken).content()))
      .isCloseTo(1.0f, org.assertj.core.data.Percentage.withPercentage(0.01))
    assertThat(magnitudeOf(model.embed(oneToken.repeat(999)).content()))
      .isCloseTo(1.0f, org.assertj.core.data.Percentage.withPercentage(0.01))
  }
}
