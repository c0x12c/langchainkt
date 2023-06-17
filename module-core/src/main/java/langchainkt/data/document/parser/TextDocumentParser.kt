package langchainkt.data.document.parser

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import langchainkt.data.document.Document
import langchainkt.data.document.DocumentParser
import langchainkt.data.document.DocumentType
import langchainkt.data.document.Metadata

class TextDocumentParser(
  private val documentType: DocumentType,
  private val charset: Charset = StandardCharsets.UTF_8
) : DocumentParser {

  override fun parse(inputStream: InputStream): Document {
    return try {
      val buffer = ByteArrayOutputStream()
      var nRead: Int
      val data = ByteArray(1024)
      while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
        buffer.write(data, 0, nRead)
      }
      buffer.flush()
      val text = String(buffer.toByteArray(), charset)
      Document.from(text, Metadata.from(Document.DOCUMENT_TYPE, documentType.toString()))
    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }
}
