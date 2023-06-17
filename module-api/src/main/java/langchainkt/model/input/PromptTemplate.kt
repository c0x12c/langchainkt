package langchainkt.model.input

import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import com.github.mustachejava.MustacheFactory
import java.io.StringReader
import java.io.StringWriter
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Collections
import langchainkt.internal.Validators

/**
 * Represents a template of a prompt that can be reused multiple times.
 * A template typically contains one or more variables (placeholders) defined as {{variable_name}} that are
 * replaced with actual values to produce a Prompt.
 * Special variables {{current_date}}, {{current_time}}, and {{current_date_time}} are automatically
 * filled with LocalDate.now(), LocalTime.now(), and LocalDateTime.now() respectively.
 * This class uses the Mustache templating engine under the hood, so all Mustache syntax and features are supported.
 */
class PromptTemplate internal constructor(
  template: String,
  private val clock: Clock = Clock.systemDefaultZone()
) {

  private val mustache: Mustache

  init {
    val stringReader = StringReader(Validators.ensureNotBlank(template, "template"))
    mustache = mustacheFactory.compile(stringReader, "template")
  }

  /**
   * Applies a value to a template containing a single variable. The single variable should have the name {{it}}.
   *
   * @param value The value that will be injected in place of the {{it}} placeholder in the template.
   * @return A Prompt object where the {{it}} placeholder in the template has been replaced by the provided value.
   */
  fun apply(value: Any): Prompt {
    return apply(Collections.singletonMap("it", value))
  }

  /**
   * Applies multiple values to a template containing multiple variables.
   *
   * @param variables A map of variable names to values that will be injected in place of the corresponding placeholders in the template.
   * @return A Prompt object where the placeholders in the template have been replaced by the provided values.
   */
  fun apply(variables: Map<String, Any>): Prompt {
    val writer = StringWriter()
    println(variables)
    mustache.execute(writer, injectDateTimeVariables(variables))
    return Prompt.from(writer.toString())
  }

  private fun injectDateTimeVariables(variables: Map<String, Any>): Map<String, Any> {
    val variablesCopy: MutableMap<String, Any> = HashMap(variables)
    variablesCopy["current_date"] = LocalDate.now(clock)
    variablesCopy["current_time"] = LocalTime.now(clock)
    variablesCopy["current_date_time"] = LocalDateTime.now(clock)
    return variablesCopy
  }

  companion object {
    private val mustacheFactory: MustacheFactory = DefaultMustacheFactory()

    fun from(template: String): PromptTemplate {
      return PromptTemplate(template)
    }
  }
}
