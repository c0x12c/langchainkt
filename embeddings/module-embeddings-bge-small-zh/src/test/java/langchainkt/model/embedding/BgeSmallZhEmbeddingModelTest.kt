package langchainkt.model.embedding

import langchainkt.data.embedding.Embedding
import langchainkt.model.embedding.VectorUtils.magnitudeOf
import langchainkt.store.embedding.CosineSimilarity.between
import langchainkt.store.embedding.RelevanceScore.fromCosineSimilarity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class BgeSmallZhEmbeddingModelTest {
  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed() {
    val model: EmbeddingModel = BgeSmallZhEmbeddingModel()
    val first = model.embed("你好").content()
    assertThat(first.vector()).hasSize(512)
    val second = model.embed("您好").content()
    assertThat(second.vector()).hasSize(512)
    val cosineSimilarity = between(first, second)
    assertThat(fromCosineSimilarity(cosineSimilarity)).isGreaterThan(0.97)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun embedding_should_have_the_same_values_as_embedding_produced_by_sentence_transformers_python_lib() {
    val model: EmbeddingModel = BgeSmallZhEmbeddingModel()
    val embedding = model.embed("书").content()
    assertThat(embedding.vector()[0]).isCloseTo(-0.0019266217f, Percentage.withPercentage(1.0))
    assertThat(embedding.vector()[1]).isCloseTo(0.0233149417f, Percentage.withPercentage(1.0))
    assertThat(embedding.vector()[510]).isCloseTo(0.0478256717f, Percentage.withPercentage(1.0))
    assertThat(embedding.vector()[511]).isCloseTo(0.0256523509f, Percentage.withPercentage(1.0))
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed_510_token_long_text() {
    val model: EmbeddingModel = BgeSmallZhEmbeddingModel()
    val oneToken = "书 "
    val embedding: Embedding = model.embed(oneToken.repeat(510)).content()
    assertThat(embedding.vector()).hasSize(512)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed_text_longer_than_510_tokens_by_splitting_and_averaging_embeddings_of_splits() {
    val model: EmbeddingModel = BgeSmallZhEmbeddingModel()
    val oneToken = "书 "
    val embedding510: Embedding = model.embed(oneToken.repeat(510)).content()
    assertThat(embedding510.vector()).hasSize(512)
    val embedding511: Embedding = model.embed(oneToken.repeat(511)).content()
    assertThat(embedding511.vector()).hasSize(512)
    val cosineSimilarity = between(embedding510, embedding511)
    assertThat(fromCosineSimilarity(cosineSimilarity)).isGreaterThan(0.99)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_produce_normalized_vectors() {
    val model: EmbeddingModel = BgeSmallZhEmbeddingModel()
    val oneToken = "书 "
    assertThat(magnitudeOf(model.embed(oneToken).content()))
      .isCloseTo(1.0f, Percentage.withPercentage(0.01))
    assertThat(magnitudeOf(model.embed(oneToken.repeat(999)).content()))
      .isCloseTo(1.0f, Percentage.withPercentage(0.01))
  }
}
