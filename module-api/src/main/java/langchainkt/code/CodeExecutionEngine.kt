package langchainkt.code

interface CodeExecutionEngine {
  fun execute(code: String): String
}
