package langchainkt

/**
 * Indicates that a class/constructor/method is experimental and might change in the future.
 */
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.CONSTRUCTOR,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
annotation class MightChangeInTheFuture(
  val value: String
)
