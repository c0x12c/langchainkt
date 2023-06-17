package langchainkt.store.embedding

import java.util.Arrays
import langchainkt.data.document.Document
import langchainkt.data.document.DocumentSplitter
import langchainkt.data.document.DocumentTransformer
import langchainkt.data.segment.TextSegment
import langchainkt.data.segment.TextSegmentTransformer
import langchainkt.model.embedding.EmbeddingModel

/**
 * EmbeddingStoreIngestor is responsible for the ingestion of documents into an embedding store.
 * It manages the entire pipeline process, from splitting the documents into text segments,
 * generating embeddings for these segments using a provided embedding model, to finally
 * storing these embeddings into an embedding store.
 * Optionally, it can also transform documents before splitting them, which can be useful if you want
 * to clean your data, format it differently, etc.
 * Additionally, it can optionally transform segments after they have been split.
 */
class EmbeddingStoreIngestor(
  private val documentTransformer: DocumentTransformer?,
  private val documentSplitter: DocumentSplitter?,
  private val textSegmentTransformer: TextSegmentTransformer?,
  private val embeddingModel: EmbeddingModel,
  private val embeddingStore: EmbeddingStore<TextSegment>
) {

  fun ingest(document: Document) {
    ingest(listOf(document))
  }

  fun ingest(vararg documents: Document) {
    ingest(listOf(*documents))
  }

  fun ingest(documents: List<Document>) {
    var documents = documents
    if (documentTransformer != null) {
      documents = documentTransformer.transformAll(documents)
    }
    var segments = documentSplitter!!.splitAll(documents)
    if (textSegmentTransformer != null) {
      segments = textSegmentTransformer.transformAll(segments)
    }
    val embeddings = embeddingModel.embedAll(segments).content()
    embeddingStore.addAll(embeddings, segments)
  }

  class Builder {
    private var documentTransformer: DocumentTransformer? = null
    private lateinit var documentSplitter: DocumentSplitter
    private var textSegmentTransformer: TextSegmentTransformer? = null
    private lateinit var embeddingModel: EmbeddingModel
    private lateinit var embeddingStore: EmbeddingStore<TextSegment>
    fun documentTransformer(documentTransformer: DocumentTransformer?): Builder {
      this.documentTransformer = documentTransformer
      return this
    }

    fun documentSplitter(documentSplitter: DocumentSplitter): Builder {
      this.documentSplitter = documentSplitter
      return this
    }

    fun textSegmentTransformer(textSegmentTransformer: TextSegmentTransformer?): Builder {
      this.textSegmentTransformer = textSegmentTransformer
      return this
    }

    fun embeddingModel(embeddingModel: EmbeddingModel): Builder {
      this.embeddingModel = embeddingModel
      return this
    }

    fun embeddingStore(embeddingStore: EmbeddingStore<TextSegment>): Builder {
      this.embeddingStore = embeddingStore
      return this
    }

    fun build(): EmbeddingStoreIngestor {
      return EmbeddingStoreIngestor(
        documentTransformer,
        documentSplitter,
        textSegmentTransformer,
        embeddingModel,
        embeddingStore
      )
    }
  }

  companion object {
    @JvmStatic
    fun builder(): Builder {
      return Builder()
    }
  }
}
