package langchainkt.model.output

import langchainkt.internal.Json

class EnumOutputParser(
  private val enumClass: Class<out Enum<*>>
) : OutputParser<Enum<*>> {

  override fun parse(text: String): Enum<*> {
    return Json.fromJson(text, enumClass)
  }

  override fun formatInstructions(): String {
    return "one of " + enumClass.getEnumConstants().contentToString()
  }
}
