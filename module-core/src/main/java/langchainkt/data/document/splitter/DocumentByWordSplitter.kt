package langchainkt.data.document.splitter

import langchainkt.data.document.DocumentSplitter
import langchainkt.model.Tokenizer

/**
 * Splits the provided [Document] into words and attempts to fit as many words as possible
 * into a single [TextSegment], adhering to the limit set by `maxSegmentSize`.
 *
 *
 * The `maxSegmentSize` can be defined in terms of characters (default) or tokens.
 * For token-based limit, a [Tokenizer] must be provided.
 *
 *
 * Word boundaries are detected by a minimum of one space (" ").
 * Any additional whitespaces before or after are ignored.
 * So, the following examples are all valid word separators: " ", "  ", "\n", and so on.
 *
 *
 * If multiple words fit within `maxSegmentSize`, they are joined together using a space (" ").
 *
 *
 * Although this should not happen, if a single word is too long and exceeds `maxSegmentSize`,
 * the `subSplitter` ([DocumentByCharacterSplitter] by default) is used to split it into smaller parts and
 * place them into multiple segments.
 * Such segments contain only the parts of the split long word.
 *
 *
 * Each [TextSegment] inherits all metadata from the [Document] and includes an "index" metadata key
 * representing its position within the document (starting from 0).
 */
class DocumentByWordSplitter : HierarchicalDocumentSplitter {
  constructor(
    maxSegmentSizeInChars: Int,
    maxOverlapSizeInChars: Int
  ) : super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, null)

  constructor(
    maxSegmentSizeInChars: Int,
    maxOverlapSizeInChars: Int,
    subSplitter: DocumentSplitter?
  ) : super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, subSplitter)

  constructor(
    maxSegmentSizeInTokens: Int,
    maxOverlapSizeInTokens: Int,
    tokenizer: Tokenizer?
  ) : super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer, null)

  constructor(
    maxSegmentSizeInTokens: Int,
    maxOverlapSizeInTokens: Int,
    tokenizer: Tokenizer?,
    subSplitter: DocumentSplitter?
  ) : super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer, subSplitter)

  public override fun split(text: String): Array<String> {
    return text
      .split("\\s+".toRegex()) // additional whitespaces are ignored
      .dropLastWhile { it.isEmpty() }
      .toTypedArray()
  }

  public override fun joinDelimiter(): String {
    return " "
  }

  override fun defaultSubSplitter(): DocumentSplitter? {
    return DocumentByCharacterSplitter(maxSegmentSize, maxOverlapSize, tokenizer)
  }
}
