package langchainkt.data.document.parser

import java.io.IOException
import java.io.InputStream
import langchainkt.data.document.Document
import langchainkt.data.document.DocumentParser
import langchainkt.data.document.DocumentType
import langchainkt.data.document.Metadata
import org.apache.poi.extractor.ExtractorFactory

/**
 * Extracts text from a Microsoft Office document.
 * This parser supports various file formats, including ppt, pptx, doc, docx, xls, and xlsx.
 * For detailed information on supported formats, please refer to the [official Apache POI website](https://poi.apache.org/).
 */
class MsOfficeDocumentParser(
  private val documentType: DocumentType
) : DocumentParser {

  override fun parse(inputStream: InputStream): Document {
    try {
      ExtractorFactory.createExtractor(inputStream).use { extractor ->
        return Document(extractor.text, Metadata.from(Document.DOCUMENT_TYPE, documentType))
      }
    } catch (e: IOException) {
      throw RuntimeException(e)
    }
  }
}
