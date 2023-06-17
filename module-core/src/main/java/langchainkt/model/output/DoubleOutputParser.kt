package langchainkt.model.output

class DoubleOutputParser : OutputParser<Double> {

  override fun parse(text: String): Double {
    return text.toDouble()
  }

  override fun formatInstructions(): String {
    return "floating point number"
  }
}
