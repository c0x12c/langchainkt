package langchainkt.model.output

class IntOutputParser : OutputParser<Int> {

  override fun parse(text: String): Int {
    return text.toInt()
  }

  override fun formatInstructions(): String {
    return "integer number"
  }
}
