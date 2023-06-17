package langchainkt.service

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class SystemMessage(
  /**
   * Prompt template can be defined in one line or multiple lines.
   * If the template is defined in multiple lines, the lines will be joined with a delimiter defined below.
   */
  vararg val value: String,
  val delimiter: String = "\n"
)
