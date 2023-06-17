package langchainkt.data.document

import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import langchainkt.data.document.DocumentLoaderUtils.load
import langchainkt.data.document.DocumentLoaderUtils.parserFor
import langchainkt.data.document.DocumentType.Companion.of
import langchainkt.data.document.source.FileSystemSource.Companion.from
import langchainkt.internal.Exceptions
import org.slf4j.LoggerFactory

object FileSystemDocumentLoader {
  private val log = LoggerFactory.getLogger(FileSystemDocumentLoader::class.java)

  /**
   * Loads a document from the specified file, detecting document type automatically.
   * See [DocumentType] for the list of supported document types.
   * If the document type is UNKNOWN, it is treated as TXT.
   *
   * @param filePath path to the file
   * @return document
   * @throws IllegalArgumentException if specified path is not a file
   */
  @JvmStatic
  fun loadDocument(filePath: String): Document {
    return loadDocument(Paths.get(filePath))
  }

  /**
   * Loads a document from the specified file, detecting document type automatically.
   * See [DocumentType] for the list of supported document types.
   * If the document type is UNKNOWN, it is treated as TXT.
   *
   * @param filePath path to the file
   * @return document
   * @throws IllegalArgumentException if specified path is not a file
   */
  @JvmStatic
  @JvmOverloads
  fun loadDocument(filePath: Path, documentType: DocumentType = of(filePath.toString())): Document {
    if (!Files.isRegularFile(filePath)) {
      throw Exceptions.illegalArgument("%s is not a file", filePath)
    }
    return load(from(filePath), parserFor(documentType))
  }

  /**
   * Loads a document from the specified file.
   *
   * @param filePath     path to the file
   * @param documentType type of the document
   * @return document
   * @throws IllegalArgumentException if specified path is not a file
   */
  @JvmStatic
  fun loadDocument(filePath: String, documentType: DocumentType): Document {
    return loadDocument(Paths.get(filePath), documentType)
  }

  /**
   * Loads documents from the specified directory. Does not use recursion.
   * Detects document types automatically.
   * See [DocumentType] for the list of supported document types.
   * If the document type is UNKNOWN, it is treated as TXT.
   *
   * @param directoryPath path to the directory with files
   * @return list of documents
   * @throws IllegalArgumentException if specified path is not a directory
   */
  @JvmStatic
  fun loadDocuments(directoryPath: Path): List<Document> {
    if (!Files.isDirectory(directoryPath)) {
      throw Exceptions.illegalArgument("%s is not a directory", directoryPath)
    }
    val documents: MutableList<Document> = ArrayList()
    try {
      Files.list(directoryPath).use { paths ->
        paths.filter { path: Path -> Files.isRegularFile(path) }
          .forEach { filePath: Path ->
            try {
              val document = loadDocument(filePath)
              documents.add(document)
            } catch (e: Exception) {
              log.warn("Failed to load document from $filePath", e)
            }
          }
      }
    } catch (e: IOException) {
      throw RuntimeException(e)
    }
    return documents
  }

  /**
   * Loads documents from the specified directory. Does not use recursion.
   * Detects document types automatically.
   * See [DocumentType] for the list of supported document types.
   * If the document type is UNKNOWN, it is treated as TXT.
   *
   * @param directoryPath path to the directory with files
   * @return list of documents
   * @throws IllegalArgumentException if specified path is not a directory
   */
  @JvmStatic
  fun loadDocuments(directoryPath: String): List<Document> {
    return loadDocuments(Paths.get(directoryPath))
  }
}
