package langchainkt.data.document

import java.io.IOException
import java.io.InputStream

/**
 * Defines the interface for a Document source.
 * Documents can be loaded from various sources such as the file system, HTTP, FTP, etc.
 */
interface DocumentSource {
  /**
   * Provides an InputStream to read the content of the document.
   * This method can be implemented to read from various sources like a local file or a network connection.
   *
   * @return An InputStream from which the document content can be read.
   * @throws IOException If an I/O error occurs while creating the InputStream.
   */
  @Throws(IOException::class)
  fun inputStream(): InputStream?

  /**
   * Returns the metadata associated with the source of the document.
   * This could include details such as the source location, date of creation, owner, etc.
   *
   * @return A Metadata object containing information associated with the source of the document.
   */
  fun metadata(): Metadata?
}
