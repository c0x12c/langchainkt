package langchainkt.agent.tool

/**
 * Parameter of a Tool
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class P(
  /**
   * Description of a parameter
   */
  val value: String
)
