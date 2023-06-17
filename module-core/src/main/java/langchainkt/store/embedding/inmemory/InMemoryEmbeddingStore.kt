package langchainkt.store.embedding.inmemory

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.Objects
import java.util.PriorityQueue
import java.util.Queue
import langchainkt.data.embedding.Embedding
import langchainkt.data.segment.TextSegment
import langchainkt.internal.Utils.randomUUID
import langchainkt.store.embedding.CosineSimilarity.between
import langchainkt.store.embedding.EmbeddingMatch
import langchainkt.store.embedding.EmbeddingStore
import langchainkt.store.embedding.RelevanceScore.fromCosineSimilarity

/**
 * An [EmbeddingStore] that stores embeddings in memory.
 *
 *
 * Uses a brute force approach by iterating over all embeddings to find the best matches.
 *
 *
 * This store can be persisted using the [.serializeToJson] and [.serializeToFile] methods.
 *
 *
 * It can also be recreated from JSON or a file using the [.fromJson] and [.fromFile] methods.
 *
 * @param <Embedded> The class of the object that has been embedded.
 * Typically, it is [langchainkt.data.segment.TextSegment].
</Embedded> */
class InMemoryEmbeddingStore<Embedded> : EmbeddingStore<Embedded> {
  private val entries: MutableList<Entry<Embedded?>> = ArrayList()
  override fun add(embedding: Embedding): String {
    val id = randomUUID()
    add(id, embedding)
    return id
  }

  override fun add(id: String, embedding: Embedding) {
    add(id, embedding, null)
  }

  override fun add(embedding: Embedding, embedded: Embedded): String {
    val id = randomUUID()
    add(id, embedding, embedded)
    return id
  }

  private fun add(id: String, embedding: Embedding, embedded: Embedded?) {
    entries.add(Entry(id, embedding, embedded))
  }

  override fun addAll(embeddings: List<Embedding>): List<String> {
    val ids: MutableList<String> = ArrayList()
    for (embedding in embeddings) {
      ids.add(add(embedding))
    }
    return ids
  }

  override fun addAll(embeddings: List<Embedding>, embedded: List<Embedded>): List<String> {
    require(embeddings.size == embedded.size) { "The list of embeddings and embedded must have the same size" }
    val ids: MutableList<String> = ArrayList()
    for (i in embeddings.indices) {
      ids.add(add(embeddings[i], embedded[i]))
    }
    return ids
  }

  override fun findRelevant(referenceEmbedding: Embedding, maxResults: Int, minScore: Double): List<EmbeddingMatch<Embedded>> {
    val matches: Queue<EmbeddingMatch<Embedded>> = PriorityQueue { left, right ->
      left.score().compareTo(right.score())
    }
    for (entry in entries) {
      val cosineSimilarity = between(entry.embedding, referenceEmbedding)
      val score = fromCosineSimilarity(cosineSimilarity)
      if (score >= minScore) {
        matches.add(EmbeddingMatch(score, entry.id, entry.embedding, entry.embedded))
        if (matches.size > maxResults) {
          matches.poll()
        }
      }
    }
    return matches.sortedBy { it.score() }.reversed()
  }

  override fun equals(o: Any?): Boolean {
    if (this === o) return true
    if (o == null || javaClass != o.javaClass) return false
    val that = o as InMemoryEmbeddingStore<*>
    return entries == that.entries
  }

  override fun hashCode(): Int {
    return Objects.hash(entries)
  }

  fun serializeToJson(): String {
    return Gson().toJson(this)
  }

  fun serializeToFile(filePath: Path?) {
    try {
      val json = serializeToJson()
      Files.write(filePath, json.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
    } catch (e: IOException) {
      throw RuntimeException(e)
    }
  }

  fun serializeToFile(filePath: String?) {
    serializeToFile(Paths.get(filePath))
  }

  private data class Entry<Embedded>(
    var id: String,
    var embedding: Embedding,
    var embedded: Embedded
  )

  companion object {
    @JvmStatic
    fun fromJson(json: String): InMemoryEmbeddingStore<TextSegment> {
      val type = object : TypeToken<InMemoryEmbeddingStore<TextSegment?>?>() {}.type
      return Gson().fromJson(json, type)
    }

    @JvmStatic
    fun fromFile(filePath: Path): InMemoryEmbeddingStore<TextSegment> {
      return try {
        val json = String(Files.readAllBytes(filePath))
        fromJson(json)
      } catch (e: IOException) {
        throw RuntimeException(e)
      }
    }

    fun fromFile(filePath: String): InMemoryEmbeddingStore<TextSegment> {
      return fromFile(Paths.get(filePath))
    }
  }
}
