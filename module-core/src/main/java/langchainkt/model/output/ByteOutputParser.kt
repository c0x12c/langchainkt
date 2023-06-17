package langchainkt.model.output

class ByteOutputParser : OutputParser<Byte> {
  override fun parse(text: String): Byte {
    return text.toByte()
  }

  override fun formatInstructions(): String {
    return "integer number in range [-128, 127]"
  }
}
