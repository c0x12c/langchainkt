package langchainkt.model.output

interface OutputParser<T> {
  fun parse(text: String): T
  fun formatInstructions(): String
}
