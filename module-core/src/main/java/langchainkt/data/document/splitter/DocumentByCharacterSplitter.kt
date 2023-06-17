package langchainkt.data.document.splitter

import langchainkt.data.document.DocumentSplitter
import langchainkt.model.Tokenizer

/**
 * Splits the provided [Document] into characters and attempts to fit as many characters as possible
 * into a single [TextSegment], adhering to the limit set by `maxSegmentSize`.
 *
 *
 * The `maxSegmentSize` can be defined in terms of characters (default) or tokens.
 * For token-based limit, a [Tokenizer] must be provided.
 *
 *
 * If multiple characters fit within `maxSegmentSize`, they are joined together without delimiters.
 *
 *
 * Each [TextSegment] inherits all metadata from the [Document] and includes an "index" metadata key
 * representing its position within the document (starting from 0).
 */
class DocumentByCharacterSplitter : HierarchicalDocumentSplitter {
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
    return text.split("".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
  }

  public override fun joinDelimiter(): String {
    return ""
  }

  override fun defaultSubSplitter(): DocumentSplitter? {
    return null
  }
}
