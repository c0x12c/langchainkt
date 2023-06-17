package langchainkt.store.embedding

import langchainkt.data.embedding.Embedding

/**
 * Represents a matched embedding along with its relevance score (derivative of cosine distance), ID, and original embedded content.
 *
 * @param <Embedded> The class of the object that has been embedded. Typically, it is [TextSegment].
</Embedded> */
data class EmbeddingMatch<Embedded>(
  val score: Double,
  val embeddingId: String,
  val embedding: Embedding,
  val embedded: Embedded?
) {

  /**
   * Returns the relevance score (derivative of cosine distance) of this embedding compared to
   * a reference embedding during a search.
   * The current implementation assumes that the embedding store uses cosine distance when comparing embeddings.
   *
   * @return Relevance score, ranging from 0 (not relevant) to 1 (highly relevant).
   */
  fun score(): Double {
    return score
  }

  /**
   * @return The ID of the embedding assigned when adding this embedding to the store.
   */
  fun embeddingId(): String {
    return embeddingId
  }

  /**
   * @return The embedding that has been matched.
   */
  fun embedding(): Embedding {
    return embedding
  }

  /**
   * @return The original content that was embedded. Typically, this is a [TextSegment].
   */
  fun embedded(): Embedded? {
    return embedded
  }

  override fun toString(): String {
    return "EmbeddingMatch {" +
      " score = " + score +
      ", embedded = " + embedded +
      ", embeddingId = " + embeddingId +
      ", embedding = " + embedding +
      " }"
  }
}
