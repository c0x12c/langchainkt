package langchainkt.agent.tool

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.typeOf
import langchainkt.agent.tool.ToolExecutionRequest.Companion.builder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ToolExecutorTest {
  private val tool = TestTool()

  class TestTool {
    @Tool
    fun doubles(arg0: Double, arg1: Double): Double {
      return arg0 + arg1
    }

    @Tool
    fun floats(arg0: Float, arg1: Float): Float {
      return arg0 + arg1
    }

    @Tool
    fun bigDecimals(arg0: BigDecimal, arg1: BigDecimal): BigDecimal {
      return arg0.add(arg1)
    }

    @Tool
    fun longs(arg0: Long, arg1: Long): Long {
      return arg0 + arg1
    }

    @Tool
    fun ints(arg0: Int, arg1: Int): Int {
      return arg0 + arg1
    }

    @Tool
    fun shorts(arg0: Short, arg1: Short): Short {
      return (arg0 + arg1).toShort()
    }

    @Tool
    fun bytes(arg0: Byte, arg1: Byte): Byte {
      return (arg0 + arg1).toByte()
    }

    @Tool
    fun bigIntegers(arg0: BigInteger, arg1: BigInteger): BigInteger {
      return arg0.add(arg1)
    }
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": 2}", "{\"arg0\": 2.0, \"arg1\": 2.0}", "{\"arg0\": 1.9, \"arg1\": 2.1}"])
  @Throws(NoSuchMethodException::class)
  fun should_execute_tool_with_parameters_of_type_double(arguments: String) {
    executeAndAssert(arguments, "doubles", typeOf<Double>(), typeOf<Double>(), "4.0")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": 2}", "{\"arg0\": 2.0, \"arg1\": 2.0}", "{\"arg0\": 1.9, \"arg1\": 2.1}"])
  @Throws(NoSuchMethodException::class)
  fun should_execute_tool_with_parameters_of_type_float(arguments: String) {
    executeAndAssert(arguments, "floats", typeOf<Float>(), typeOf<Float>(), "4.0")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": " + Float.MAX_VALUE + "}", "{\"arg0\": 2, \"arg1\": " + -Double.MAX_VALUE + "}"])
  @Throws(NoSuchMethodException::class)
  fun should_fail_when_argument_does_not_fit_into_float_type(arguments: String) {
    executeAndExpectFailure(arguments, "floats", typeOf<Float>(), typeOf<Float>(), "is out of range for the float type")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": 2}", "{\"arg0\": 2.0, \"arg1\": 2.0}", "{\"arg0\": 1.9, \"arg1\": 2.1}"])
  @Throws(NoSuchMethodException::class)
  fun should_execute_tool_with_parameters_of_type_BigDecimal(arguments: String) {
    executeAndAssert(arguments, "bigDecimals", typeOf<BigDecimal>(), typeOf<BigDecimal>(), "4.0")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": 2}", "{\"arg0\": 2.0, \"arg1\": 2.0}"])
  @Throws(NoSuchMethodException::class)
  fun should_execute_tool_with_parameters_of_type_long(arguments: String) {
    executeAndAssert(arguments, "longs", typeOf<Long>(), typeOf<Long>(), "4")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": 2.1}", "{\"arg0\": 2.1, \"arg1\": 2}"])
  @Throws(NoSuchMethodException::class)
  fun should_fail_when_argument_is_fractional_number_for_parameter_of_type_long(arguments: String) {
    executeAndExpectFailure(arguments, "longs", typeOf<Long>(), typeOf<Long>(), "argument type mismatch")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": " + Double.MAX_VALUE + "}", "{\"arg0\": 2, \"arg1\": " + -Double.MAX_VALUE + "}"])
  @Throws(NoSuchMethodException::class)
  fun should_fail_when_argument_does_not_fit_into_long_type(arguments: String) {
    executeAndExpectFailure(arguments, "longs", typeOf<Long>(), typeOf<Long>(), "is out of range for the long type")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": 2}", "{\"arg0\": 2.0, \"arg1\": 2.0}"])
  @Throws(NoSuchMethodException::class)
  fun should_execute_tool_with_parameters_of_type_int(arguments: String) {
    executeAndAssert(arguments, "ints", typeOf<Int>(), typeOf<Int>(), "4")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": 2.1}", "{\"arg0\": 2.1, \"arg1\": 2}"])
  @Throws(NoSuchMethodException::class)
  fun should_fail_when_argument_is_fractional_number_for_parameter_of_type_int(arguments: String) {
    executeAndExpectFailure(arguments, "ints", typeOf<Int>(), typeOf<Int>(), "argument type mismatch")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": " + Double.MAX_VALUE + "}", "{\"arg0\": 2, \"arg1\": " + -Double.MAX_VALUE + "}"])
  @Throws(NoSuchMethodException::class)
  fun should_fail_when_argument_does_not_fit_into_int_type(arguments: String) {
    executeAndExpectFailure(arguments, "ints", typeOf<Int>(), typeOf<Int>(), "is out of range for the integer type")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": 2}", "{\"arg0\": 2.0, \"arg1\": 2.0}"])
  @Throws(NoSuchMethodException::class)
  fun should_execute_tool_with_parameters_of_type_short(arguments: String) {
    executeAndAssert(arguments, "shorts", typeOf<Short>(), typeOf<Short>(), "4")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": 2.1}", "{\"arg0\": 2.1, \"arg1\": 2}"])
  @Throws(NoSuchMethodException::class)
  fun should_fail_when_argument_is_fractional_number_for_parameter_of_type_short(arguments: String) {
    executeAndExpectFailure(arguments, "shorts", typeOf<Short>(), typeOf<Short>(), "argument type mismatch")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": " + Double.MAX_VALUE + "}", "{\"arg0\": 2, \"arg1\": " + -Double.MAX_VALUE + "}"])
  @Throws(NoSuchMethodException::class)
  fun should_fail_when_argument_does_not_fit_into_short_type(arguments: String) {
    executeAndExpectFailure(arguments, "shorts", typeOf<Short>(), typeOf<Short>(), "is out of range for the short type")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": 2}", "{\"arg0\": 2.0, \"arg1\": 2.0}"])
  @Throws(NoSuchMethodException::class)
  fun should_execute_tool_with_parameters_of_type_byte(arguments: String) {
    executeAndAssert(arguments, "bytes", typeOf<Byte>(), typeOf<Byte>(), "4")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": 2.1}", "{\"arg0\": 2.1, \"arg1\": 2}"])
  @Throws(NoSuchMethodException::class)
  fun should_fail_when_argument_is_fractional_number_for_parameter_of_type_byte(arguments: String) {
    executeAndExpectFailure(arguments, "bytes", typeOf<Byte>(), typeOf<Byte>(), "argument type mismatch")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": " + Double.MAX_VALUE + "}", "{\"arg0\": 2, \"arg1\": " + -Double.MAX_VALUE + "}"])
  @Throws(NoSuchMethodException::class)
  fun should_fail_when_argument_does_not_fit_into_byte_type(arguments: String) {
    executeAndExpectFailure(arguments, "bytes", typeOf<Byte>(), typeOf<Byte>(), "is out of range for the byte type")
  }

  @ParameterizedTest
  @ValueSource(strings = ["{\"arg0\": 2, \"arg1\": 2}", "{\"arg0\": 2.0, \"arg1\": 2.0}"])
  @Throws(NoSuchMethodException::class)
  fun should_execute_tool_with_parameters_of_type_BigInteger(arguments: String) {
    executeAndAssert(arguments, "bigIntegers", typeOf<BigInteger>(), typeOf<BigInteger>(), "4")
  }

  @Throws(NoSuchMethodException::class)
  private fun executeAndAssert(
    arguments: String,
    methodName: String,
    arg0Type: KType,
    arg1Type: KType,
    expectedResult: String
  ) {
    val request = builder()
      .arguments(arguments)
      .build()
    val executor = ToolExecutor(tool, functionOf<TestTool>(methodName, arg0Type, arg1Type))
    val result = executor.execute(request)
    assertThat(result).isEqualTo(expectedResult)
  }

  @Throws(NoSuchMethodException::class)
  private fun executeAndExpectFailure(
    arguments: String,
    methodName: String,
    arg0Type: KType,
    arg1Type: KType,
    expectedError: String
  ) {
    val request = builder()
      .arguments(arguments)
      .build()
    val executor = ToolExecutor(tool, functionOf<TestTool>(methodName, arg0Type, arg1Type))
    assertThatThrownBy { executor.execute(request) }
      .isExactlyInstanceOf(IllegalArgumentException::class.java)
      .hasMessageContaining(expectedError)
  }

  private inline fun <reified T : Any> functionOf(
    name: String,
    argumentType0: KType,
    argumentType1: KType
  ): KFunction<*> {
    return T::class.declaredMemberFunctions.firstOrNull {
      val parameterTypes = it.parameters.map { p -> p.type }
      it.name == name && parameterTypes[1] == argumentType0 && parameterTypes[2] == argumentType1
    } ?: throw NoSuchMethodException()
  }
}
