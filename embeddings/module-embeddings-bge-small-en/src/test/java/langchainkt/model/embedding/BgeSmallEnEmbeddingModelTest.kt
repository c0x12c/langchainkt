package langchainkt.model.embedding

import langchainkt.model.embedding.VectorUtils.magnitudeOf
import langchainkt.store.embedding.CosineSimilarity.between
import langchainkt.store.embedding.RelevanceScore.fromCosineSimilarity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class BgeSmallEnEmbeddingModelTest {
  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed() {
    val model: EmbeddingModel = BgeSmallEnEmbeddingModel()
    val first = model.embed("hi").content()
    assertThat(first.vector()).hasSize(384)
    val second = model.embed("hello").content()
    assertThat(second.vector()).hasSize(384)
    val cosineSimilarity = between(first, second)
    assertThat(fromCosineSimilarity(cosineSimilarity)).isGreaterThan(0.97)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun embedding_should_have_the_same_values_as_embedding_produced_by_sentence_transformers_python_lib() {
    val model: EmbeddingModel = BgeSmallEnEmbeddingModel()
    val embedding = model.embed("I love cool flags!").content()
    assertThat(embedding.vector()[0]).isCloseTo(-0.0440094993f, Percentage.withPercentage(1.0))
    assertThat(embedding.vector()[1]).isCloseTo(0.0160218030f, Percentage.withPercentage(1.0))
    assertThat(embedding.vector()[382]).isCloseTo(-0.0074426383f, Percentage.withPercentage(1.0))
    assertThat(embedding.vector()[383]).isCloseTo(-0.0055019930f, Percentage.withPercentage(1.0))
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed_510_token_long_text() {
    val model: EmbeddingModel = BgeSmallEnEmbeddingModel()
    val oneToken = "hello "
    val embedding = model.embed(oneToken.repeat(510)).content()
    assertThat(embedding.vector()).hasSize(384)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed_text_longer_than_510_tokens_by_splitting_and_averaging_embeddings_of_splits() {
    val model: EmbeddingModel = BgeSmallEnEmbeddingModel()
    val oneToken = "hello "
    val embedding510 = model.embed(oneToken.repeat(510)).content()
    assertThat(embedding510.vector()).hasSize(384)
    val embedding511 = model.embed(oneToken.repeat(511)).content()
    assertThat(embedding511.vector()).hasSize(384)
    val cosineSimilarity = between(embedding510, embedding511)
    assertThat(fromCosineSimilarity(cosineSimilarity)).isGreaterThan(0.99)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_produce_normalized_vectors() {
    val model: EmbeddingModel = BgeSmallEnEmbeddingModel()
    val oneToken = "hello "
    assertThat(magnitudeOf(model.embed(oneToken).content()))
      .isCloseTo(1.0f, Percentage.withPercentage(0.01))
    assertThat(magnitudeOf(model.embed(oneToken.repeat(999)).content()))
      .isCloseTo(1.0f, Percentage.withPercentage(0.01))
  }
}
