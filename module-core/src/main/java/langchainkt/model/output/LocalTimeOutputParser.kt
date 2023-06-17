package langchainkt.model.output

import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeOutputParser : OutputParser<LocalTime> {

  override fun parse(text: String): LocalTime {
    return LocalTime.parse(text, DateTimeFormatter.ISO_LOCAL_TIME)
  }

  override fun formatInstructions(): String {
    return "23:59:59"
  }
}
