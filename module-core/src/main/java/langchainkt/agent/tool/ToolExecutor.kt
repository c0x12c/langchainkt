package langchainkt.agent.tool

import java.lang.reflect.InvocationTargetException
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.math.floor
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.typeOf
import langchainkt.internal.Json.toJson
import org.slf4j.LoggerFactory

class ToolExecutor(
  private val any: Any,
  private val method: KFunction<*>
) {

  fun execute(request: ToolExecutionRequest): String? {
    log.debug("About to execute {}", request)
    val arguments = argumentsFor(request.argumentsAsMap())
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
    return if (method.returnType == typeOf<Unit>()) {
      "Success"
    } else {
      toJson(result)
    }
  }

  private fun argumentsFor(argumentsMap: Map<String, Any>): Array<Any?> {
    // Drop the first parameter, which is the tool name
    val parameters = method.parameters.drop(1)
    val arguments = arrayOfNulls<Any>(parameters.size)
    for (i in parameters.indices) {
      val parameterName = parameters[i].name
      if (argumentsMap.containsKey(parameterName)) {
        var argument = argumentsMap[parameterName]
        val parameterType = parameters[i].type
        // Gson always parses numbers into the Double type. If the parameter type is not Double, a conversion attempt is made.
        if (argument is Double && parameterType != typeOf<Double>()) {
          val doubleValue = argument
          if (parameterType == typeOf<Float>()) {
            require(!(doubleValue < -Float.MAX_VALUE || doubleValue > Float.MAX_VALUE)) { "Double value $doubleValue is out of range for the float type" }
            argument = doubleValue.toFloat()
          } else if (parameterType == typeOf<BigDecimal>()) {
            argument = BigDecimal.valueOf(doubleValue)
          }

          // Allow conversion to integer types only if double value has no fractional part
          if (hasNoFractionalPart(doubleValue)) {
            when (parameterType) {
              typeOf<Int>() -> {
                require(!(doubleValue < Int.MIN_VALUE || doubleValue > Int.MAX_VALUE)) {
                  "Double value $doubleValue is out of range for the integer type"
                }
                argument = doubleValue.toInt()
              }

              typeOf<Long>() -> {
                require(!(doubleValue < Long.MIN_VALUE || doubleValue > Long.MAX_VALUE)) {
                  "Double value $doubleValue is out of range for the long type"
                }
                argument = doubleValue.toLong()
              }

              typeOf<Short>() -> {
                require(!(doubleValue < Short.MIN_VALUE || doubleValue > Short.MAX_VALUE)) {
                  "Double value $doubleValue is out of range for the short type"
                }
                println("short")
                argument = doubleValue.toInt().toShort()
              }

              typeOf<Byte>() -> {
                require(!(doubleValue < Byte.MIN_VALUE || doubleValue > Byte.MAX_VALUE)) {
                  "Double value $doubleValue is out of range for the byte type"
                }
                argument = doubleValue.toInt().toByte()
              }

              typeOf<BigInteger>() -> {
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
