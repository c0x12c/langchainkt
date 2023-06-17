package langchainkt.service

/**
 * When a method in the AI Service is annotated with @Moderate, each invocation of this method will call not only the LLM,
 * but also the moderation model (which must be provided during the construction of the AI Service) in parallel.
 * This ensures that no malicious content is supplied by the user.
 * Before the method returns an answer from the LLM, it will wait until the moderation model returns a result.
 * If the moderation model flags the content, a ModerationException will be thrown.
 * There is also an option to moderate user input *before* sending it to the LLM. If you require this functionality,
 * please open an issue.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class Moderate
