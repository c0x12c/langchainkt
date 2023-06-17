package langchainkt.data.document

import java.util.Locale

enum class DocumentType(
  vararg supportedExtensions: String
) {

  TXT(".txt"),
  PDF(".pdf"),
  HTML(".html", ".htm", ".xhtml"),
  DOC(".doc", ".docx"),
  XLS(".xls", ".xlsx"),
  PPT(".ppt", ".pptx"),
  UNKNOWN;

  private val supportedExtensions: Iterable<String>

  init {
    this.supportedExtensions = listOf(*supportedExtensions)
  }

  companion object {
    @JvmStatic
    fun of(fileName: String): DocumentType {
      for (documentType in entries) {
        for (supportedExtension in documentType.supportedExtensions) {
          if (fileName.lowercase(Locale.getDefault()).endsWith(supportedExtension)) {
            return documentType
          }
        }
      }
      return UNKNOWN
    }
  }
}
