package langchainkt.store.embedding.pinecone

import com.google.protobuf.Struct
import com.google.protobuf.Value
import io.pinecone.PineconeClient
import io.pinecone.PineconeClientConfig
import io.pinecone.PineconeConnection
import io.pinecone.PineconeConnectionConfig
import io.pinecone.proto.FetchRequest
import io.pinecone.proto.QueryRequest
import io.pinecone.proto.QueryVector
import io.pinecone.proto.UpsertRequest
import io.pinecone.proto.Vector
import langchainkt.data.embedding.Embedding
import langchainkt.data.embedding.Embedding.Companion.from
import langchainkt.data.segment.TextSegment
import langchainkt.internal.Utils.randomUUID
import langchainkt.store.embedding.CosineSimilarity.between
import langchainkt.store.embedding.EmbeddingMatch
import langchainkt.store.embedding.EmbeddingStore
import langchainkt.store.embedding.RelevanceScore.fromCosineSimilarity

/**
 * Represents a [Pinecone](https://www.pinecone.io/) index as an embedding store.
 * Current implementation assumes the index uses the cosine distance metric.
 * Does not support storing [langchainkt.data.document.Metadata] yet.
 *
 * Creates an instance of PineconeEmbeddingStore.
 *
 * @param apiKey      The Pinecone API key.
 * @param environment The environment (e.g., "northamerica-northeast1-gcp").
 * @param projectId   The ID of the project (e.g., "19a129b"). This is **not** a project name.
 * The ID can be found in the Pinecone URL: https://app.pinecone.io/organizations/.../projects/...:{projectId}/indexes.
 * @param index       The name of the index (e.g., "test").
 * @param nameSpace   (Optional) Namespace. If not provided, "default" will be used.
 */
class PineconeEmbeddingStore(
  apiKey: String?,
  environment: String?,
  projectId: String?,
  index: String?,
  nameSpace: String?
) : EmbeddingStore<TextSegment> {
  private val connection: PineconeConnection
  private val nameSpace: String


  init {
    val configuration = PineconeClientConfig()
      .withApiKey(apiKey)
      .withEnvironment(environment)
      .withProjectName(projectId)
    val pineconeClient = PineconeClient(configuration)
    val connectionConfig = PineconeConnectionConfig()
      .withIndexName(index)
    connection = pineconeClient.connect(connectionConfig)
    this.nameSpace = nameSpace ?: DEFAULT_NAMESPACE
  }

  override fun add(embedding: Embedding): String {
    val id = randomUUID()
    add(id, embedding)
    return id
  }

  override fun add(id: String, embedding: Embedding) {
    addInternal(id, embedding, null)
  }

  override fun add(embedding: Embedding, embedded: TextSegment): String {
    val id = randomUUID()
    addInternal(id, embedding, embedded)
    return id
  }

  override fun addAll(embeddings: List<Embedding>): List<String> {
    val ids = embeddings.map { randomUUID() }
    addAllInternal(ids, embeddings, null)
    return ids
  }

  override fun addAll(embeddings: List<Embedding>, embedded: List<TextSegment>): List<String> {
    val ids = embeddings.map { randomUUID() }
    addAllInternal(ids, embeddings, embedded)
    return ids
  }

  private fun addInternal(id: String, embedding: Embedding, textSegment: TextSegment?) {
    addAllInternal(listOf(id), listOf(embedding), if (textSegment == null) null else listOf(textSegment))
  }

  private fun addAllInternal(ids: List<String>, embeddings: List<Embedding>, textSegments: List<TextSegment>?) {
    val upsertRequestBuilder = UpsertRequest.newBuilder()
      .setNamespace(nameSpace)
    for (i in embeddings.indices) {
      val id = ids[i]
      val embedding = embeddings[i]
      val vectorBuilder = Vector.newBuilder()
        .setId(id)
        .addAllValues(embedding.vectorAsList())
      if (textSegments != null) {
        vectorBuilder.setMetadata(Struct.newBuilder()
          .putFields(METADATA_TEXT_SEGMENT, Value.newBuilder()
            .setStringValue(textSegments[i].text())
            .build()))
      }
      upsertRequestBuilder.addVectors(vectorBuilder.build())
    }
    connection.blockingStub.upsert(upsertRequestBuilder.build())
  }

  override fun findRelevant(referenceEmbedding: Embedding, maxResults: Int, minScore: Double): List<EmbeddingMatch<TextSegment>> {
    val queryVector = QueryVector
      .newBuilder()
      .addAllValues(referenceEmbedding.vectorAsList())
      .setTopK(maxResults)
      .setNamespace(nameSpace)
      .build()

    val queryRequest = QueryRequest
      .newBuilder()
      .addQueries(queryVector)
      .setTopK(maxResults)
      .build()

    val matchedVectorIds = connection.blockingStub
      .query(queryRequest)
      .resultsList[0]
      .matchesList
      .map { it.id }

    if (matchedVectorIds.isEmpty()) {
      return emptyList()
    }
    val matchedVectors: Collection<Vector> = connection.blockingStub
      .fetch(
        FetchRequest.newBuilder()
          .addAllIds(matchedVectorIds)
          .setNamespace(nameSpace)
          .build()
      )
      .vectorsMap
      .values

    return matchedVectors
      .map { vector: Vector -> toEmbeddingMatch(vector, referenceEmbedding) }
      .filter { it.score >= minScore }
      .sortedBy { it.score }
      .reversed()
  }

  class Builder {
    private var apiKey: String? = null
    private var environment: String? = null
    private var projectId: String? = null
    private var index: String? = null
    private var nameSpace: String? = null

    /**
     * @param apiKey The Pinecone API key.
     */
    fun apiKey(apiKey: String?): Builder {
      this.apiKey = apiKey
      return this
    }

    /**
     * @param environment The environment (e.g., "northamerica-northeast1-gcp").
     */
    fun environment(environment: String?): Builder {
      this.environment = environment
      return this
    }

    /**
     * @param projectId The ID of the project (e.g., "19a129b"). This is **not** a project name.
     * The ID can be found in the Pinecone URL: https://app.pinecone.io/organizations/.../projects/...:{projectId}/indexes.
     */
    fun projectId(projectId: String?): Builder {
      this.projectId = projectId
      return this
    }

    /**
     * @param index The name of the index (e.g., "test").
     */
    fun index(index: String?): Builder {
      this.index = index
      return this
    }

    /**
     * @param nameSpace (Optional) Namespace. If not provided, "default" will be used.
     */
    fun nameSpace(nameSpace: String?): Builder {
      this.nameSpace = nameSpace
      return this
    }

    fun build(): PineconeEmbeddingStore {
      return PineconeEmbeddingStore(apiKey, environment, projectId, index, nameSpace)
    }
  }

  companion object {
    private const val DEFAULT_NAMESPACE = "default" // do not change, will break backward compatibility!
    private const val METADATA_TEXT_SEGMENT = "text_segment" // do not change, will break backward compatibility!
    private fun toEmbeddingMatch(vector: Vector, referenceEmbedding: Embedding): EmbeddingMatch<TextSegment> {
      val textSegmentValue = vector.metadata
        .fieldsMap[METADATA_TEXT_SEGMENT]
      val embedding = from(vector.valuesList)
      val cosineSimilarity = between(embedding, referenceEmbedding)
      return EmbeddingMatch(
        fromCosineSimilarity(cosineSimilarity),
        vector.getId(),
        embedding,
        if (textSegmentValue == null) null else TextSegment.from(textSegmentValue.getStringValue())
      )
    }

    fun builder(): Builder {
      return Builder()
    }
  }
}
