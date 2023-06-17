package langchainkt.data.document.splitter

import java.lang.String.format
import java.util.concurrent.atomic.AtomicInteger
import langchainkt.data.document.Document
import langchainkt.data.document.DocumentSplitter
import langchainkt.data.segment.TextSegment
import langchainkt.internal.Utils
import langchainkt.internal.Validators
import langchainkt.model.Tokenizer

abstract class HierarchicalDocumentSplitter protected constructor(
  maxSegmentSizeInTokens: Int,
  maxOverlapSizeInTokens: Int,
  @JvmField protected val tokenizer: Tokenizer?,
  subSplitter: DocumentSplitter? = null
) : DocumentSplitter {

  @JvmField
  protected val maxSegmentSize: Int

  @JvmField
  protected val maxOverlapSize: Int
  private val subSplitter: DocumentSplitter?

  protected constructor(
    maxSegmentSizeInChars: Int,
    maxOverlapSizeInChars: Int
  ) : this(maxSegmentSizeInChars, maxOverlapSizeInChars, null, null)

  protected constructor(
    maxSegmentSizeInChars: Int,
    maxOverlapSizeInChars: Int,
    subSplitter: HierarchicalDocumentSplitter?
  ) : this(maxSegmentSizeInChars, maxOverlapSizeInChars, null, subSplitter)

  init {
    maxSegmentSize = Validators.ensureGreaterThanZero(maxSegmentSizeInTokens, "maxSegmentSize")
    maxOverlapSize = Validators.ensureBetween(maxOverlapSizeInTokens, 0, maxSegmentSize, "maxOverlapSize")
    this.subSplitter = subSplitter ?: defaultSubSplitter()
  }

  protected abstract fun split(text: String): Array<String>
  protected abstract fun joinDelimiter(): String
  protected abstract fun defaultSubSplitter(): DocumentSplitter?

  override fun split(document: Document): List<TextSegment> {
    val segments: MutableList<TextSegment> = ArrayList()
    val segmentBuilder = SegmentBuilder(maxSegmentSize, { text: String -> sizeOf(text) }, joinDelimiter())
    val index = AtomicInteger(0)
    val parts = split(document.text())
    var overlap: String? = null
    for (part in parts) {
      if (segmentBuilder.hasSpaceFor(part)) {
        segmentBuilder.append(part)
      } else {
        if (segmentBuilder.isNotEmpty && segmentBuilder.build() != overlap) {
          val segmentText = segmentBuilder.build()
          segments.add(createSegment(segmentText, document, index.getAndIncrement()))
          segmentBuilder.reset()
          overlap = overlapFrom(segmentText)
          segmentBuilder.append(overlap)
        }
        if (segmentBuilder.hasSpaceFor(part)) {
          segmentBuilder.append(part)
        } else {
          if (subSplitter == null) {
            throw RuntimeException(format(
              "The text \"%s...\" (%s %s long) doesn't fit into the maximum segment size (%s %s), " +
                "and there is no subSplitter defined to split it further.",
              Utils.firstChars(part, 30),
              sizeOf(part), if (tokenizer == null) "characters" else "tokens",
              maxSegmentSize, if (tokenizer == null) "characters" else "tokens"
            ))
          }
          segmentBuilder.append(part)
          for (segment in subSplitter.split(Document.from(segmentBuilder.build()))) {
            segments.add(createSegment(segment.text(), document, index.getAndIncrement()))
          }
          segmentBuilder.reset()
          val lastSegment = segments[segments.size - 1]
          overlap = overlapFrom(lastSegment.text())
          segmentBuilder.append(overlap)
        }
      }
    }
    if (segmentBuilder.isNotEmpty && segmentBuilder.build() != overlap) {
      segments.add(createSegment(segmentBuilder.build(), document, index.getAndIncrement()))
    }
    return segments
  }

  private fun overlapFrom(segmentText: String): String {
    if (maxOverlapSize == 0) {
      return ""
    }
    val overlapBuilder = SegmentBuilder(maxOverlapSize, { text: String -> sizeOf(text) }, joinDelimiter())
    val sentences = DocumentBySentenceSplitter(1, 0, null, null).split(segmentText)
    for (i in sentences.indices.reversed()) {
      val part = sentences[i]
      if (overlapBuilder.hasSpaceFor(part)) {
        overlapBuilder.prepend(part)
      } else {
        return overlapBuilder.build()
      }
    }
    return ""
  }

  private fun sizeOf(text: String): Int {
    return tokenizer?.estimateTokenCountInText(text) ?: text.length
  }

  companion object {
    private const val INDEX = "index"
    private fun createSegment(text: String, document: Document, index: Int): TextSegment {
      val metadata = document.metadata().copy().add(INDEX, index)
      return TextSegment.from(text, metadata)
    }
  }
}
