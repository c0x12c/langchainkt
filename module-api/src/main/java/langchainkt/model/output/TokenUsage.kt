package langchainkt.model.output

import langchainkt.internal.Utils

data class TokenUsage(
  private val inputTokenCount: Int? = null,
  private val outputTokenCount: Int? = null,
  private val totalTokenCount: Int? = sum(inputTokenCount, outputTokenCount)
) {
  fun inputTokenCount(): Int? {
    return inputTokenCount
  }

  fun outputTokenCount(): Int? {
    return outputTokenCount
  }

  fun totalTokenCount(): Int? {
    return totalTokenCount
  }

  fun add(that: TokenUsage): TokenUsage {
    return TokenUsage(
      sum(inputTokenCount, that.inputTokenCount),
      sum(outputTokenCount, that.outputTokenCount),
      sum(totalTokenCount, that.totalTokenCount)
    )
  }

  companion object {
    private fun sum(first: Int?, second: Int?): Int? {
      return if (first == null && second == null) {
        null
      } else {
        Utils.getOrDefault(first, 0)!! + Utils.getOrDefault(second, 0)!!
      }
    }
  }
}
