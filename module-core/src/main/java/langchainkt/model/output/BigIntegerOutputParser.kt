package langchainkt.model.output

import java.math.BigInteger

class BigIntegerOutputParser : OutputParser<BigInteger> {
  override fun parse(text: String): BigInteger {
    return BigInteger(text)
  }

  override fun formatInstructions(): String {
    return "integer number"
  }
}
