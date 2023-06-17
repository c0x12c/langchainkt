package langchainkt.service

/**
 * The values of method parameters annotated with @V, together with prompt templates defined by @UserMessage
 * and @SystemMessage, are used to produce a message that will be sent to the LLM.
 * Variables (placeholders), like {{xxx}} in prompt templates, are filled with the corresponding values
 * of parameters annotated with @V("xxx").
 *
 *
 * Example:
 * <pre>
 * `@UserMessage("Hello, my name is {{name}}. I am {{age}} years old.")`
 * String chat(@V("name") String name, @V("age") int age);
</pre> *
 *
 *
 * This annotation is necessary only when the "-parameters" option is *not* enabled during Java compilation.
 * If the "-parameters" option is enabled, parameter names can directly serve as identifiers, eliminating
 * the need to define a value of @V annotation.
 * Example:
 * <pre>
 * `@UserMessage("Hello, my name is {{name}}. I am {{age}} years old.")`
 * String chat(@V String name, @V int age);
</pre> *
 *
 *
 * When using Spring Boot, defining the value of this annotation is not required.
 *
 * @see UserMessage
 *
 * @see SystemMessage
 *
 * @see langchainkt.model.input.PromptTemplate
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class V(
  /**
   * Name of a variable (placeholder) in a prompt template.
   */
  val value: String
)
