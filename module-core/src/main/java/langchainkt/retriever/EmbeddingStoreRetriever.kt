package langchainkt.retriever

import langchainkt.data.segment.TextSegment
import langchainkt.model.embedding.EmbeddingModel
import langchainkt.store.embedding.EmbeddingMatch
import langchainkt.store.embedding.EmbeddingStore

class EmbeddingStoreRetriever(
  private val embeddingStore: EmbeddingStore<TextSegment>,
  private val embeddingModel: EmbeddingModel,
  private val maxResults: Int,
  private val minScore: Double?
) : Retriever<TextSegment> {

  override fun findRelevant(text: String): List<TextSegment> {
    val embeddedText = embeddingModel.embed(text).content()
    val relevant: List<EmbeddingMatch<TextSegment>> = if (minScore == null) {
      embeddingStore.findRelevant(embeddedText, maxResults)
    } else {
      embeddingStore.findRelevant(embeddedText, maxResults, minScore)
    }
    return relevant.mapNotNull { it.embedded() }
  }

  companion object {
    fun from(embeddingStore: EmbeddingStore<TextSegment>, embeddingModel: EmbeddingModel): EmbeddingStoreRetriever {
      return EmbeddingStoreRetriever(embeddingStore, embeddingModel, 2, null)
    }

    fun from(embeddingStore: EmbeddingStore<TextSegment>, embeddingModel: EmbeddingModel, maxResults: Int): EmbeddingStoreRetriever {
      return EmbeddingStoreRetriever(embeddingStore, embeddingModel, maxResults, null)
    }

    fun from(embeddingStore: EmbeddingStore<TextSegment>, embeddingModel: EmbeddingModel, maxResults: Int, minScore: Double): EmbeddingStoreRetriever {
      return EmbeddingStoreRetriever(embeddingStore, embeddingModel, maxResults, minScore)
    }
  }
}
