package langchainkt.model.output

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDateTimeOutputParser : OutputParser<LocalDateTime> {

  override fun parse(text: String): LocalDateTime {
    return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  }

  override fun formatInstructions(): String {
    return "2023-12-31T23:59:59"
  }
}
