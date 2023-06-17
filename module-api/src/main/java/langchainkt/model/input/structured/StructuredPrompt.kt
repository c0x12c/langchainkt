package langchainkt.model.input.structured

/**
 * Prompt template can be defined in one line or multiple lines.
 * If the template is defined in multiple lines, the lines will be joined with a delimiter defined below.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class StructuredPrompt(
  vararg val value: String,
  val delimiter: String = "\n"
)
