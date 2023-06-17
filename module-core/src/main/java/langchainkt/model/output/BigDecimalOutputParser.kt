package langchainkt.model.output

import java.math.BigDecimal

class BigDecimalOutputParser : OutputParser<BigDecimal> {

  override fun parse(text: String): BigDecimal {
    return BigDecimal(text)
  }

  override fun formatInstructions(): String {
    return "floating point number"
  }
}
