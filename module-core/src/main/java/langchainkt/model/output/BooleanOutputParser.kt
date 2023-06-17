package langchainkt.model.output

class BooleanOutputParser : OutputParser<Boolean> {
  override fun parse(text: String): Boolean {
    return text.toBoolean()
  }

  override fun formatInstructions(): String {
    return "one of [true, false]"
  }
}
