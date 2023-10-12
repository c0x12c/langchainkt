package langchainkt.model.embedding

import langchainkt.model.embedding.VectorUtils.magnitudeOf
import langchainkt.store.embedding.RelevanceScore
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Percentage
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class AllMiniLmL6V2EmbeddingModelTest {

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed() {
    val model: EmbeddingModel = AllMiniLmL6V2EmbeddingModel()
    val first = model.embed("hi").content()
    assertThat(first.vector()).hasSize(384)
    val second = model.embed("hello").content()
    assertThat(second.vector()).hasSize(384)
    val cosineSimilarity = langchainkt.store.embedding.CosineSimilarity.between(first, second)
    assertThat(RelevanceScore.fromCosineSimilarity(cosineSimilarity)).isGreaterThan(0.9)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun embedding_should_have_the_same_values_as_embedding_produced_by_sentence_transformers_python_lib() {
    val model: EmbeddingModel = AllMiniLmL6V2EmbeddingModel()
    val embedding = model.embed("I love sentence transformers.").content()
    assertThat(embedding.vector()[0]).isCloseTo(-0.0803190097f, Percentage.withPercentage(1.0))
    assertThat(embedding.vector()[1]).isCloseTo(-0.0171345081f, Percentage.withPercentage(1.0))
    assertThat(embedding.vector()[382]).isCloseTo(0.0478825271f, Percentage.withPercentage(1.0))
    assertThat(embedding.vector()[383]).isCloseTo(-0.0561899580f, Percentage.withPercentage(1.0))
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed_510_token_long_text() {
    val model: EmbeddingModel = AllMiniLmL6V2EmbeddingModel()
    val oneToken = "hello "
    val embedding: langchainkt.data.embedding.Embedding = model.embed(oneToken.repeat(510)).content()
    assertThat(embedding.vector()).hasSize(384)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_fail_to_embed_511_token_long_text() {
    val model: EmbeddingModel = AllMiniLmL6V2EmbeddingModel()
    val oneToken = "hello "
    val embedding510: langchainkt.data.embedding.Embedding = model.embed(oneToken.repeat(510)).content()
    assertThat(embedding510.vector()).hasSize(384)
    val embedding511: langchainkt.data.embedding.Embedding = model.embed(oneToken.repeat(511)).content()
    assertThat(embedding511.vector()).hasSize(384)
    val cosineSimilarity = langchainkt.store.embedding.CosineSimilarity.between(embedding510, embedding511)
    assertThat(RelevanceScore.fromCosineSimilarity(cosineSimilarity)).isGreaterThan(0.99)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_produce_normalized_vectors() {
    val model: EmbeddingModel = AllMiniLmL6V2EmbeddingModel()
    val oneToken = "hello "
    assertThat(magnitudeOf(model.embed(oneToken).content()))
      .isCloseTo(1.0f, Percentage.withPercentage(0.01))
    assertThat(magnitudeOf(model.embed(oneToken.repeat(999)).content()))
      .isCloseTo(1.0f, Percentage.withPercentage(0.01))
  }
}
