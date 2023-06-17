package langchainkt.model.output

class FloatOutputParser : OutputParser<Float> {

  override fun parse(text: String): Float {
    return text.toFloat()
  }

  override fun formatInstructions(): String {
    return "floating point number"
  }
}
