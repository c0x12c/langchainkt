package langchainkt.code

import java.time.Duration
import langchainkt.agent.tool.P
import langchainkt.agent.tool.Tool
import langchainkt.code.JavaScriptCodeFixer.fixIfNoLogToConsole

/**
 * A tool that executes JS code using the Judge0 service, hosted by Rapid API.
 * Constructs a new instance with the provided Rapid API key, a flag to control whether to fix the code, and a timeout.
 *
 * @param apiKey          Rapid API key. You can subscribe to the free plan (Basic) here: https://rapidapi.com/judge0-official/api/judge0-ce/pricing
 * @param fixCodeIfNeeded Judge0 can return result of an execution if it was printed to the console.
 * If provided JS code does not print result to the console, attempt will be made to fix it.
 * @param timeout         Timeout for calling Judge0.
 */
class Judge0JavaScriptExecutionTool @JvmOverloads constructor(
  apiKey: String,
  fixCodeIfNeeded: Boolean = true,
  timeout: Duration = Duration.ofSeconds(10)
) {

  private val engine: Judge0JavaScriptEngine
  private val fixCodeIfNeeded: Boolean

  init {
    engine = Judge0JavaScriptEngine(apiKey, JAVASCRIPT, timeout)
    this.fixCodeIfNeeded = fixCodeIfNeeded
  }

  @Tool("MUST be used for accurate calculations: math, sorting, filtering, aggregating, string processing, etc")
  fun executeJavaScriptCode(@P("JavaScript code to execute, result MUST be printed to console") javaScriptCode: String): String {
    var src = javaScriptCode
    if (fixCodeIfNeeded) {
      src = fixIfNoLogToConsole(src)
    }
    return engine.execute(src)
  }

  companion object {
    private const val JAVASCRIPT = 93
  }
}
