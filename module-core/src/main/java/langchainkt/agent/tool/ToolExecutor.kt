package langchainkt.agent.tool

import java.lang.reflect.InvocationTargetException
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.floor
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.isAccessible
import langchainkt.internal.Json.toJson
import org.slf4j.LoggerFactory

class ToolExecutor(
  private val any: Any,
  private val method: KFunction<*>
) {

  fun execute(request: ToolExecutionRequest): String? {
    log.debug("About to execute {}", request)
    val arguments = prepareArguments(request.argumentsAsMap())
    return try {
      val result = execute(arguments)
      log.debug("Tool execution result: {}", result)
      result
    } catch (e: IllegalAccessException) {
      try {
        method.isAccessible = true
        val result = execute(arguments)
        log.debug("Tool execution result: {}", result)
        result
      } catch (e2: IllegalAccessException) {
        throw RuntimeException(e2)
      } catch (e2: InvocationTargetException) {
        val cause = e2.cause
        log.error("Error while executing tool", cause)
        cause!!.message
      }
    } catch (e: InvocationTargetException) {
      val cause = e.cause
      log.error("Error while executing tool", cause)
      cause!!.message
    }
  }

  @Throws(IllegalAccessException::class, InvocationTargetException::class)
  private fun execute(arguments: Array<Any?>): String {
    val result = method.call(any, *arguments)
    return if (method.returnType == Void.TYPE) {
      "Success"
    } else toJson(result)
  }

  private fun prepareArguments(argumentsMap: Map<String, Any>): Array<Any?> {
    val parameters = method.parameters
    val arguments = arrayOfNulls<Any>(parameters.size)
    for (i in parameters.indices) {
      val parameterName = parameters[i].name
      if (argumentsMap.containsKey(parameterName)) {
        var argument = argumentsMap[parameterName]
        val parameterType = parameters[i].type
        // Gson always parses numbers into the Double type. If the parameter type is not Double, a conversion attempt is made.
        if (argument is Double && !(parameterType == Double::class.java || parameterType == Double::class.javaPrimitiveType)) {
          val doubleValue = argument
          if (parameterType == Float::class.java || parameterType == Float::class.javaPrimitiveType) {
            require(!(doubleValue < -Float.MAX_VALUE || doubleValue > Float.MAX_VALUE)) { "Double value $doubleValue is out of range for the float type" }
            argument = doubleValue.toFloat()
          } else if (parameterType == BigDecimal::class.java) {
            argument = BigDecimal.valueOf(doubleValue)
          }

          // Allow conversion to integer types only if double value has no fractional part
          if (hasNoFractionalPart(doubleValue)) {
            when (parameterType) {
              Int::class.java, Int::class -> {
                require(!(doubleValue < Int.MIN_VALUE || doubleValue > Int.MAX_VALUE)) { "Double value $doubleValue is out of range for the integer type" }
                argument = doubleValue.toInt()
              }
              Long::class.java, Long::class -> {
                require(!(doubleValue < Long.MIN_VALUE || doubleValue > Long.MAX_VALUE)) { "Double value $doubleValue is out of range for the long type" }
                argument = doubleValue.toLong()
              }
              Short::class.java, Short::class -> {
                require(!(doubleValue < Short.MIN_VALUE || doubleValue > Short.MAX_VALUE)) { "Double value $doubleValue is out of range for the short type" }
                argument = doubleValue.toInt()
              }
              Byte::class.java, Byte::class -> {
                require(!(doubleValue < Byte.MIN_VALUE || doubleValue > Byte.MAX_VALUE)) { "Double value $doubleValue is out of range for the byte type" }
                argument = doubleValue.toInt()
              }
              BigInteger::class.java -> {
                argument = BigDecimal.valueOf(doubleValue).toBigInteger()
              }
            }
          }
        }
        arguments[i] = argument
      }
    }
    return arguments
  }

  companion object {
    private val log = LoggerFactory.getLogger(ToolExecutor::class.java)
    private fun hasNoFractionalPart(doubleValue: Double): Boolean {
      return doubleValue == floor(doubleValue)
    }
  }
}
