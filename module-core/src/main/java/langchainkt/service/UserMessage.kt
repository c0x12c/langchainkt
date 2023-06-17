package langchainkt.service

@Retention(AnnotationRetention.RUNTIME)
@Target(
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER,
  AnnotationTarget.VALUE_PARAMETER
)
annotation class UserMessage(
  /**
   * Prompt template can be defined in one line or multiple lines.
   * If the template is defined in multiple lines, the lines will be joined with a delimiter defined below.
   */
  vararg val value: String = [""],
  val delimiter: String = "\n"
)
