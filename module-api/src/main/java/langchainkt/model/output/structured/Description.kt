package langchainkt.model.output.structured

/**
 * The description can be defined in one line or multiple lines.
 * If the description is defined in multiple lines, the lines will be joined with a space (" ") automatically.
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Description(
  vararg val value: String
)
