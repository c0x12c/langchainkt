package langchainkt.data.document.parser

import java.io.IOException
import java.io.InputStream
import langchainkt.data.document.Document
import langchainkt.data.document.DocumentParser
import langchainkt.data.document.DocumentType
import langchainkt.data.document.Metadata
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

class PdfDocumentParser : DocumentParser {

  override fun parse(inputStream: InputStream): Document {
    return try {
      val pdfDocument = PDDocument.load(inputStream)
      val stripper = PDFTextStripper()
      val content = stripper.getText(pdfDocument)
      pdfDocument.close()
      Document.from(content, Metadata.from(Document.DOCUMENT_TYPE, DocumentType.PDF))
    } catch (e: IOException) {
      throw RuntimeException(e)
    }
  }
}
