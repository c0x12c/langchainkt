package langchainkt.code

import org.slf4j.LoggerFactory

internal object JavaScriptCodeFixer {

  private val log = LoggerFactory.getLogger(JavaScriptCodeFixer::class.java)

  @JvmStatic
  fun fixIfNoLogToConsole(code: String): String {
    return if (code.contains("\n")) {
      fixIfNoLogToConsole(code, "\n")
    } else {
      fixIfNoLogToConsole(code, " ")
    }
  }

  private fun fixIfNoLogToConsole(code: String, separator: String): String {
    val parts = code.split(separator.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val lastPart = parts[parts.size - 1]
    if (lastPart.startsWith("console.log")) {
      return code
    }
    parts[parts.size - 1] = "console.log(" + lastPart.replace(";", "") + ");"
    val fixedCode = java.lang.String.join(separator, *parts)
    log.debug("The following code \"{}\" was fixed: \"{}\"", code, fixedCode)
    return fixedCode
  }
}
