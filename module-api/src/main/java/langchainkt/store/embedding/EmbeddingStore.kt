package langchainkt.store.embedding

import langchainkt.data.embedding.Embedding

/**
 * Represents a store for embeddings, also known as a vector database.
 *
 * @param <Embedded> The class of the object that has been embedded. Typically, this is [TextSegment].
</Embedded> */
interface EmbeddingStore<E> {

  /**
   * Adds a given embedding to the store.
   *
   * @param embedding The embedding to be added to the store.
   * @return The auto-generated ID associated with the added embedding.
   */
  fun add(embedding: Embedding): String

  /**
   * Adds a given embedding to the store.
   *
   * @param id        The unique identifier for the embedding to be added.
   * @param embedding The embedding to be added to the store.
   */
  fun add(id: String, embedding: Embedding)

  /**
   * Adds a given embedding and the corresponding content that has been embedded to the store.
   *
   * @param embedding The embedding to be added to the store.
   * @param embedded  Original content that was embedded.
   * @return The auto-generated ID associated with the added embedding.
   */
  fun add(embedding: Embedding, embedded: E): String

  /**
   * Adds multiple embeddings to the store.
   *
   * @param embeddings A list of embeddings to be added to the store.
   * @return A list of auto-generated IDs associated with the added embeddings.
   */
  fun addAll(embeddings: List<Embedding>): List<String>

  /**
   * Adds multiple embeddings and their corresponding contents that have been embedded to the store.
   *
   * @param embeddings A list of embeddings to be added to the store.
   * @param embedded   A list of original contents that were embedded.
   * @return A list of auto-generated IDs associated with the added embeddings.
   */
  fun addAll(embeddings: List<Embedding>, embedded: List<E>): List<String>

  /**
   * Finds the most relevant (closest in space) embeddings to the provided reference embedding.
   * By default, minScore is set to 0, which means that the results may include embeddings with low relevance.
   *
   * @param referenceEmbedding The embedding used as a reference. Returned embeddings should be relevant (closest) to this one.
   * @param maxResults         The maximum number of embeddings to be returned.
   * @return A list of embedding matches.
   * Each embedding match includes a relevance score (derivative of cosine distance),
   * ranging from 0 (not relevant) to 1 (highly relevant).
   */
  fun findRelevant(referenceEmbedding: Embedding, maxResults: Int): List<EmbeddingMatch<E>> {
    return findRelevant(referenceEmbedding, maxResults, 0.0)
  }

  /**
   * Finds the most relevant (closest in space) embeddings to the provided reference embedding.
   *
   * @param referenceEmbedding The embedding used as a reference. Returned embeddings should be relevant (closest) to this one.
   * @param maxResults         The maximum number of embeddings to be returned.
   * @param minScore           The minimum relevance score, ranging from 0 to 1 (inclusive).
   * Only embeddings with a score of this value or higher will be returned.
   * @return A list of embedding matches.
   * Each embedding match includes a relevance score (derivative of cosine distance),
   * ranging from 0 (not relevant) to 1 (highly relevant).
   */
  fun findRelevant(referenceEmbedding: Embedding, maxResults: Int, minScore: Double): List<EmbeddingMatch<E>>
}
