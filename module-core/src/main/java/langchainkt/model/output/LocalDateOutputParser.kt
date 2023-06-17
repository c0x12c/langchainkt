package langchainkt.model.output

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateOutputParser : OutputParser<LocalDate> {

  override fun parse(text: String): LocalDate {
    return LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE)
  }

  override fun formatInstructions(): String {
    return "2023-12-31"
  }
}
