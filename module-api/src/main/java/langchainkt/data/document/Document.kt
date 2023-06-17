package langchainkt.data.document

import java.util.Objects
import langchainkt.data.segment.TextSegment
import langchainkt.internal.Utils
import langchainkt.internal.Validators

/**
 * Represents an unstructured piece of text that usually corresponds to a content of a single file.
 * This text could originate from various sources such as a text file, PDF, DOCX, or a web page (HTML).
 * Each document may have associated metadata including its source, owner, creation date, etc.
 */
class Document(text: String?, metadata: Metadata) {
  private val text: String
  private val metadata: Metadata

  init {
    this.text = Validators.ensureNotBlank(text, "text")
    this.metadata = Validators.ensureNotNull(metadata, "metadata")
  }

  fun text(): String {
    return text
  }

  fun metadata(): Metadata {
    return metadata
  }

  fun metadata(key: String?): String? {
    return metadata[key!!]
  }

  fun toTextSegment(): TextSegment {
    return TextSegment.from(text, metadata.copy().add("index", 0))
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val that = other as Document
    return text == that.text && metadata == that.metadata
  }

  override fun hashCode(): Int {
    return Objects.hash(text, metadata)
  }

  override fun toString(): String {
    return "Document {" +
      " text = " + Utils.quoted(text) +
      " metadata = " + metadata.asMap() +
      " }"
  }

  companion object {
    const val DOCUMENT_TYPE = "document_type"
    const val FILE_NAME = "file_name"
    const val ABSOLUTE_DIRECTORY_PATH = "absolute_directory_path"
    const val URL = "url"
    @JvmStatic
    fun from(text: String?): Document {
      return Document(text, Metadata())
    }

    @JvmStatic
    fun from(text: String?, metadata: Metadata): Document {
      return Document(text, metadata)
    }

    fun document(text: String?): Document {
      return from(text)
    }

    fun document(text: String?, metadata: Metadata): Document {
      return from(text, metadata)
    }
  }
}
