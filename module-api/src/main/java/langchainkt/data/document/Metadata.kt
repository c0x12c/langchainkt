package langchainkt.data.document

import java.util.Objects

/**
 * Represents metadata of a Document or a TextSegment.
 * The metadata is stored in a key-value map, where both keys and values are strings.
 * For a Document, the metadata could include information such as the source, creation date,
 * owner, or any other relevant details.
 * For a TextSegment, in addition to metadata copied from a document, it can also include segment-specific information,
 * such as the page number, the position of the segment within the document, chapter, etc.
 */
class Metadata(
  private val metadata: MutableMap<String, String> = mutableMapOf()
) {

  operator fun get(key: String): String? {
    return metadata[key]
  }

  fun add(key: String, value: Any): Metadata {
    metadata[key] = value.toString()
    return this
  }

  fun remove(key: String?): Metadata {
    metadata.remove(key)
    return this
  }

  fun copy(): Metadata {
    return Metadata(HashMap(metadata))
  }

  fun asMap(): Map<String, String> {
    return HashMap(metadata)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val that = other as Metadata
    return metadata == that.metadata
  }

  override fun hashCode(): Int {
    return Objects.hash(metadata)
  }

  override fun toString(): String {
    return "Metadata {" +
      " metadata = " + metadata +
      " }"
  }

  companion object {
    @JvmStatic
    fun from(key: String, value: Any): Metadata {
      return Metadata().add(key, value)
    }

    @JvmStatic
    fun from(metadata: Map<String, String>): Metadata {
      return Metadata(metadata.toMutableMap())
    }

    @JvmStatic
    fun metadata(key: String, value: Any): Metadata {
      return from(key, value)
    }
  }
}
