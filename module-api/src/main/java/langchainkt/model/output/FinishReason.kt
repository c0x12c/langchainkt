package langchainkt.model.output

enum class FinishReason {
  STOP,
  LENGTH,
  TOOL_EXECUTION,
  CONTENT_FILTER
}
