package langchainkt.model.output

class ShortOutputParser : OutputParser<Short> {

  override fun parse(text: String): Short {
    return text.toShort()
  }

  override fun formatInstructions(): String {
    return "integer number in range [-32768, 32767]"
  }
}
