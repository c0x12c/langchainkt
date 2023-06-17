package langchainkt.model.language

import langchainkt.data.segment.TextSegment
import langchainkt.model.input.Prompt

/**
 * Represents an interface for estimating the count of tokens in various text types such as a text, prompt, text segment, etc.
 * This can be useful when it's necessary to know in advance the cost of processing a specified text by the LLM.
 */
interface TokenCountEstimator {

  fun estimateTokenCount(text: String): Int

  fun estimateTokenCount(prompt: Prompt): Int {
    return estimateTokenCount(prompt.text())
  }

  fun estimateTokenCount(textSegment: TextSegment): Int {
    return estimateTokenCount(textSegment.text())
  }
}
