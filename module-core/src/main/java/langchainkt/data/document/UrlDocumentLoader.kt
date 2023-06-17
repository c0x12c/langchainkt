package langchainkt.data.document

import java.net.MalformedURLException
import java.net.URL
import langchainkt.data.document.DocumentLoaderUtils.load
import langchainkt.data.document.DocumentLoaderUtils.parserFor
import langchainkt.data.document.DocumentType.Companion.of
import langchainkt.data.document.source.UrlSource.Companion.from

object UrlDocumentLoader {
  /**
   * Loads a document from the specified URL, detecting the document type automatically.
   * See [DocumentType] for the list of supported document types.
   * If the document type is UNKNOWN, it is treated as TXT.
   *
   * @param url URL of the file
   * @return document
   * @throws RuntimeException if specified URL is malformed
   */
  @JvmStatic
  fun load(url: String): Document {
    return try {
      load(URL(url))
    } catch (e: MalformedURLException) {
      throw RuntimeException(e)
    }
  }

  /**
   * Loads a document from the specified URL, detecting the document type automatically.
   * See [DocumentType] for the list of supported document types.
   * If the document type is UNKNOWN, it is treated as TXT.
   *
   * @param url URL of the file
   * @return document
   */
  @JvmOverloads
  fun load(url: URL, documentType: DocumentType = of(url.toString())): Document {
    return load(from(url), parserFor(documentType))
  }

  /**
   * Loads a document from the specified URL.
   *
   * @param url          URL of the file
   * @param documentType type of the document
   * @return document
   * @throws RuntimeException if specified URL is malformed
   */
  fun load(url: String?, documentType: DocumentType): Document {
    return try {
      load(URL(url), documentType)
    } catch (e: MalformedURLException) {
      throw RuntimeException(e)
    }
  }
}
