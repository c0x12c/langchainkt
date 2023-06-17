package langchainkt.data.document

import langchainkt.data.document.parser.MsOfficeDocumentParser
import langchainkt.data.document.parser.PdfDocumentParser
import langchainkt.data.document.parser.TextDocumentParser

internal object DocumentLoaderUtils {

  @JvmStatic
  fun load(source: DocumentSource, parser: DocumentParser): Document {
    try {
      source.inputStream().use { inputStream ->
        val document = parser.parse(inputStream!!)
        source.metadata()!!.asMap().forEach { (key: String?, value: String?) -> document.metadata().add(key, value) }
        return document
      }
    } catch (e: Exception) {
      throw RuntimeException("Failed to load document", e)
    }
  }

  @JvmStatic
  fun parserFor(type: DocumentType?): DocumentParser {
    return when (type) {
      DocumentType.TXT, DocumentType.HTML, DocumentType.UNKNOWN -> TextDocumentParser(type)
      DocumentType.PDF -> PdfDocumentParser()
      DocumentType.DOC, DocumentType.XLS, DocumentType.PPT -> MsOfficeDocumentParser(type)
      else -> throw RuntimeException(String.format("Cannot find parser for document type '%s'", type))
    }
  }
}
