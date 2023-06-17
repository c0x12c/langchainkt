package langchainkt.service

/**
 * The value of a method parameter annotated with @MemoryId will be used to find the memory belonging to that user/conversation.
 * A parameter annotated with @MemoryId can be of any type, provided it has properly implemented equals() and hashCode() methods.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class MemoryId
