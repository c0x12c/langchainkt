package langchainkt.model.embedding

import langchainkt.model.embedding.VectorUtils.magnitudeOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class E5SmallV2EmbeddingModelTest {

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed() {
    val model: EmbeddingModel = E5SmallV2EmbeddingModel()
    val first = model.embed("query: hi").content()
    assertThat(first.vector()).hasSize(384)
    val second = model.embed("query: hello").content()
    assertThat(second.vector()).hasSize(384)
    val cosineSimilarity = langchainkt.store.embedding.CosineSimilarity.between(first, second)
    assertThat(langchainkt.store.embedding.RelevanceScore.fromCosineSimilarity(cosineSimilarity)).isGreaterThan(0.98)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun embedding_should_have_the_same_values_as_embedding_produced_by_transformers_python_lib() {
    val model: EmbeddingModel = E5SmallV2EmbeddingModel()
    val embedding = model.embed("query: I love transformers.").content()
    assertThat(embedding.vector()[0]).isCloseTo(-0.0663562790f, org.assertj.core.data.Percentage.withPercentage(1.0))
    assertThat(embedding.vector()[1]).isCloseTo(0.0153982891f, org.assertj.core.data.Percentage.withPercentage(1.0))
    assertThat(embedding.vector()[382]).isCloseTo(-0.0412562378f, org.assertj.core.data.Percentage.withPercentage(1.0))
    assertThat(embedding.vector()[383]).isCloseTo(-0.0130311009f, org.assertj.core.data.Percentage.withPercentage(1.0))
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed_510_token_long_text() {
    val model: EmbeddingModel = E5SmallV2EmbeddingModel()
    val oneToken = "hello "
    val embedding: langchainkt.data.embedding.Embedding = model.embed(oneToken.repeat(510)).content()
    assertThat(embedding.vector()).hasSize(384)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_embed_text_longer_than_510_tokens_by_splitting_and_averaging_embeddings_of_splits() {
    val model: EmbeddingModel = E5SmallV2EmbeddingModel()
    val oneToken = "hello "
    val embedding510: langchainkt.data.embedding.Embedding = model.embed(oneToken.repeat(510)).content()
    assertThat(embedding510.vector()).hasSize(384)
    val embedding511: langchainkt.data.embedding.Embedding = model.embed(oneToken.repeat(511)).content()
    assertThat(embedding511.vector()).hasSize(384)
    val cosineSimilarity = langchainkt.store.embedding.CosineSimilarity.between(embedding510, embedding511)
    assertThat(langchainkt.store.embedding.RelevanceScore.fromCosineSimilarity(cosineSimilarity)).isGreaterThan(0.99)
  }

  @Test
  @Disabled("Temporary disabling. This test should run only when this or used (e.g. langchain4j-embeddings) module(s) change")
  fun should_produce_normalized_vectors() {
    val model: EmbeddingModel = E5SmallV2EmbeddingModel()
    val oneToken = "hello "
    assertThat(magnitudeOf(model.embed(oneToken).content()))
      .isCloseTo(1.0f, org.assertj.core.data.Percentage.withPercentage(0.01))
    assertThat(magnitudeOf(model.embed(oneToken.repeat(999)).content()))
      .isCloseTo(1.0f, org.assertj.core.data.Percentage.withPercentage(0.01))
  }
}
