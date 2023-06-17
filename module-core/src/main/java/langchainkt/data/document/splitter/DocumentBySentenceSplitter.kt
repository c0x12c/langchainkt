package langchainkt.data.document.splitter

import langchainkt.data.document.DocumentSplitter
import langchainkt.model.Tokenizer
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel

/**
 * Splits the provided [Document] into sentences and attempts to fit as many sentences as possible
 * into a single [TextSegment], adhering to the limit set by `maxSegmentSize`.
 *
 *
 * The `maxSegmentSize` can be defined in terms of characters (default) or tokens.
 * For token-based limit, a [Tokenizer] must be provided.
 *
 *
 * Sentence boundaries are detected using the Apache OpenNLP library with the English sentence model.
 *
 *
 * If multiple sentences fit within `maxSegmentSize`, they are joined together using a space (" ").
 *
 *
 * If a single sentence is too long and exceeds `maxSegmentSize`,
 * the `subSplitter` ([DocumentByWordSplitter] by default) is used to split it into smaller parts and
 * place them into multiple segments.
 * Such segments contain only the parts of the split long sentence.
 *
 *
 * Each [TextSegment] inherits all metadata from the [Document] and includes an "index" metadata key
 * representing its position within the document (starting from 0).
 */
class DocumentBySentenceSplitter : HierarchicalDocumentSplitter {
  private val sentenceDetector: SentenceDetectorME

  constructor(
    maxSegmentSizeInChars: Int,
    maxOverlapSizeInChars: Int
  ) : super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, null) {
    sentenceDetector = createSentenceDetector()
  }

  constructor(
    maxSegmentSizeInChars: Int,
    maxOverlapSizeInChars: Int,
    subSplitter: DocumentSplitter?
  ) : super(maxSegmentSizeInChars, maxOverlapSizeInChars, null, subSplitter) {
    sentenceDetector = createSentenceDetector()
  }

  constructor(
    maxSegmentSizeInTokens: Int,
    maxOverlapSizeInTokens: Int,
    tokenizer: Tokenizer?
  ) : super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer, null) {
    sentenceDetector = createSentenceDetector()
  }

  constructor(
    maxSegmentSizeInTokens: Int,
    maxOverlapSizeInTokens: Int,
    tokenizer: Tokenizer?,
    subSplitter: DocumentSplitter?
  ) : super(maxSegmentSizeInTokens, maxOverlapSizeInTokens, tokenizer, subSplitter) {
    sentenceDetector = createSentenceDetector()
  }

  private fun createSentenceDetector(): SentenceDetectorME {
    val sentenceModelFilePath = "/opennlp/opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin"
    try {
      javaClass.getResourceAsStream(sentenceModelFilePath).use { `is` -> return SentenceDetectorME(SentenceModel(`is`)) }
    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }

  public override fun split(text: String): Array<String> {
    return sentenceDetector.sentDetect(text)
  }

  public override fun joinDelimiter(): String {
    return " "
  }

  override fun defaultSubSplitter(): DocumentSplitter? {
    return DocumentByWordSplitter(maxSegmentSize, maxOverlapSize, tokenizer)
  }
}
