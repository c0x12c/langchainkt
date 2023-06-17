package langchainkt.model.input

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import langchainkt.model.input.PromptTemplate.Companion.from
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class PromptTemplateTest {
  @Test
  fun should_create_prompt_from_template_with_single_variable() {
    val promptTemplate = from("My name is {{it}}.")
    val text = promptTemplate.apply("Klaus").text()
    assertThat(text).isEqualTo("My name is Klaus.")
  }

  @Test
  fun should_create_prompt_from_template_with_multiple_variables() {
    val promptTemplate = from("My name is {{name}} {{surname}}.")
    val variables: MutableMap<String, Any> = HashMap()
    variables["name"] = "Klaus"
    variables["surname"] = "Heißler"
    val text = promptTemplate.apply(variables).text()
    assertThat(text).isEqualTo("My name is Klaus Heißler.")
  }

  @Test
  fun should_provide_date_automatically() {
    val promptTemplate = from("My name is {{it}} and today is {{current_date}}")
    val text = promptTemplate.apply("Klaus").text()
    assertThat(text).isEqualTo("My name is Klaus and today is " + LocalDate.now())
  }

  @Test
  fun should_provide_time_automatically() {
    val clock = Clock.fixed(Instant.now(), ZoneOffset.UTC)
    val promptTemplate = PromptTemplate("My name is {{it}} and now is {{current_time}}", clock)
    val text = promptTemplate.apply("Klaus").text()
    assertThat(text).isEqualTo("My name is Klaus and now is " + LocalTime.now(clock))
  }

  @Test
  fun should_provide_date_and_time_automatically() {
    val clock = Clock.fixed(Instant.now(), ZoneOffset.UTC)
    val promptTemplate = PromptTemplate("My name is {{it}} and now is {{current_date_time}}", clock)
    val text = promptTemplate.apply("Klaus").text()
    assertThat(text).isEqualTo("My name is Klaus and now is " + LocalDateTime.now(clock))
  }
}
