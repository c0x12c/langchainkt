package langchainkt.model.output

class LongOutputParser : OutputParser<Long> {

  override fun parse(text: String): Long {
    return text.toLong()
  }

  override fun formatInstructions(): String {
    return "integer number"
  }
}
