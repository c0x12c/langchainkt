package langchainkt

/**
 * Indicates that a class/constructor/method is planned to change soon.
 */
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.CONSTRUCTOR,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER
)
annotation class WillChangeSoon(val value: String)
