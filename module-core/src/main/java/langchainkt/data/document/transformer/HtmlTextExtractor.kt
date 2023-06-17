package langchainkt.data.document.transformer

import java.util.stream.Collectors
import langchainkt.data.document.Document
import langchainkt.data.document.DocumentTransformer
import org.jsoup.Jsoup
import org.jsoup.internal.StringUtil
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor

/**
 * Extracts text from a given HTML document.
 * A CSS selector can be specified to extract text only from desired element(s).
 * Also, multiple CSS selectors can be specified to extract metadata from desired elements.
 *
 * Constructs an instance of HtmlToTextTransformer that extracts text from HTML elements matching the provided CSS selector.
 *
 * @param cssSelector          A CSS selector.
 * For example, "#page-content" will extract text from the HTML element with the id "page-content".
 * @param metadataCssSelectors A mapping from metadata keys to CSS selectors.
 * For example, Mep.of("title", "#page-title") will extract all text from the HTML element
 * with id "title" and store it in [Metadata] under the key "title".
 * @param includeLinks         Specifies whether links should be included in the extracted text.
*/
class HtmlTextExtractor(
  private val cssSelector: String? = null,
  private val metadataCssSelectors: Map<String, String>? = null,
  private val includeLinks: Boolean = false
) : DocumentTransformer {

  override fun transform(document: Document): Document {
    val html = document.text()
    val jsoupDocument = Jsoup.parse(html)
    val text: String = if (cssSelector != null) {
      extractText(jsoupDocument, cssSelector, includeLinks)
    } else {
      extractText(jsoupDocument, includeLinks)
    }
    val metadata = document.metadata().copy()
    metadataCssSelectors?.forEach { (metadataKey: String?, cssSelector: String?) -> metadata.add(metadataKey, jsoupDocument.select(cssSelector).text()) }
    return Document.from(text, metadata)
  }

  // taken from https://github.com/jhy/jsoup/blob/master/src/main/java/org/jsoup/examples/HtmlToPlainText.java
  private class TextExtractingVisitor(private val includeLinks: Boolean) : NodeVisitor {
    private val textBuilder = StringBuilder()
    override fun head(node: Node, depth: Int) { // hit when the node is first seen
      val name = node.nodeName()
      if (node is TextNode) textBuilder.append(node.text()) else if (name == "li") textBuilder.append("\n * ") else if (name == "dt") textBuilder.append("  ") else if (StringUtil.`in`(name, "p", "h1", "h2", "h3", "h4", "h5", "h6", "tr")) textBuilder.append("\n")
    }

    override fun tail(node: Node, depth: Int) { // hit when all the node's children (if any) have been visited
      val name = node.nodeName()
      if (StringUtil.`in`(name, "br", "dd", "dt", "p", "h1", "h2", "h3", "h4", "h5", "h6")) textBuilder.append("\n") else if (includeLinks && name == "a") textBuilder.append(String.format(" <%s>", node.absUrl("href")))
    }

    override fun toString(): String {
      return textBuilder.toString()
    }
  }

  companion object {
    private fun extractText(jsoupDocument: org.jsoup.nodes.Document, cssSelector: String, includeLinks: Boolean): String {
      return jsoupDocument.select(cssSelector)
        .joinToString("\n\n") { element: Element -> extractText(element, includeLinks) }
    }

    private fun extractText(element: Element, includeLinks: Boolean): String {
      val visitor: NodeVisitor = TextExtractingVisitor(includeLinks)
      NodeTraversor.traverse(visitor, element)
      return visitor.toString().trim { it <= ' ' }
    }
  }
}
