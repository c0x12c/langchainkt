package langchainkt.data.document.splitter

import langchainkt.data.document.DocumentSplitter
import langchainkt.model.Tokenizer

/**
 * Splits the provided [Document] into parts using the provided `regex` and attempts to fit as many parts
 * as possible into a single [TextSegment], adhering to the limit set by `maxSegmentSize`.
 *
 *
 * The `maxSegmentSize` can be defined in terms of characters (default) or tokens.
 * For token-based limit, a [Tokenizer] must be provided.
 *
 *
 * If multiple parts fit within `maxSegmentSize`, they are joined together using the provided `joinDelimiter`.
 *
 *
 * If a single part is too long and exceeds `maxSegmentSize`, the `subSplitter` (which should be provided)
 * is used to split it into sub-parts and place them into multiple segments.
 * Such segments contain only the sub-parts of the split long part.
 *
 *
 * Each [TextSegment] inherits all metadata from the [Document] and includes an "index" metadata key
 * representing its position within the document (starting from 0).
 */
class DocumentByRegexSplitter : HierarchicalDocumentSplitter {
  private val joinDelimiter: String
  private val regex: String

  constructor(
    regex: String,
    joinDelimiter: String,
    maxSegmentSizeInChars: Int,
    maxOverlapSizeInChars: Int
  ) : super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, null) {
    this.regex = regex
    this.joinDelimiter = joinDelimiter
  }

  constructor(
    regex: String,
    joinDelimiter: String,
    maxSegmentSizeInChars: Int,
    maxOverlapSizeInChars: Int,
    subSplitter: DocumentSplitter?
  ) : super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, subSplitter) {
    this.regex = regex
    this.joinDelimiter = joinDelimiter
  }

  constructor(
    regex: String,
    joinDelimiter: String,
    maxSegmentSizeInTokens: Int,
    maxOverlapSizeInTokens: Int,
    tokenizer: Tokenizer?
  ) : super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer, null) {
    this.regex = regex
    this.joinDelimiter = joinDelimiter
  }

  constructor(
    regex: String,
    joinDelimiter: String,
    maxSegmentSizeInTokens: Int,
    maxOverlapSizeInTokens: Int,
    tokenizer: Tokenizer?,
    subSplitter: DocumentSplitter?
  ) : super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer, subSplitter) {
    this.regex = regex
    this.joinDelimiter = joinDelimiter
  }

  public override fun split(text: String): Array<String> {
    return text.split(regex.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
  }

  public override fun joinDelimiter(): String {
    return joinDelimiter
  }

  override fun defaultSubSplitter(): DocumentSplitter? {
    return null
  }
}
