package langchainkt.service

/**
 * The value of a method parameter annotated with @UserName will be injected into the field 'name' of a UserMessage.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class UserName
