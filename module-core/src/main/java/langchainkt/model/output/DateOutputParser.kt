package langchainkt.model.output

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

class DateOutputParser : OutputParser<Date> {
  override fun parse(text: String): Date {
    return try {
      SIMPLE_DATE_FORMAT.parse(text)
    } catch (e: ParseException) {
      throw RuntimeException(e)
    }
  }

  override fun formatInstructions(): String {
    return DATE_PATTERN
  }

  companion object {
    private const val DATE_PATTERN = "yyyy-MM-dd"
    private val SIMPLE_DATE_FORMAT = SimpleDateFormat(DATE_PATTERN)
  }
}
