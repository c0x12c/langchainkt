package langchainkt.model.embedding

import langchainkt.data.segment.TextSegment

/**
 * Represents an interface for estimating the count of tokens in various texts, text segments, etc.
 * This can be useful when it's necessary to know in advance the cost of processing a specified text by the LLM.
 */
interface TokenCountEstimator {

  fun estimateTokenCount(text: String): Int

  fun estimateTokenCount(segment: TextSegment): Int {
    return estimateTokenCount(segment.text())
  }

  fun estimateTokenCount(segments: List<TextSegment>): Int {
    var tokenCount = 0
    for (textSegment in segments) {
      tokenCount += estimateTokenCount(textSegment)
    }
    return tokenCount
  }
}
