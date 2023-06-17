package langchainkt.agent.tool

import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.javaType
import kotlin.reflect.typeOf
import langchainkt.agent.tool.JsonSchemaProperty.Companion.description
import langchainkt.agent.tool.JsonSchemaProperty.Companion.enums
import langchainkt.agent.tool.ToolSpecification.Companion.builder
import langchainkt.internal.Utils

object ToolSpecifications {

  fun toolSpecificationsFrom(objectWithTools: Any): List<ToolSpecification> {
    return objectWithTools::class.memberFunctions
      .filter { it.hasAnnotation<Tool>() }
      .map { toolSpecificationFrom(it) }
  }

  fun toolSpecificationFrom(method: KFunction<*>): ToolSpecification {
    val annotation = method.findAnnotation<Tool>()!!
    val name = if (Utils.isNullOrBlank(annotation.name)) method.name else annotation.name
    val description = java.lang.String.join("\n", *annotation.value)
    val builder = builder()
      .name(name)
      .description(description)
    for (parameter in method.parameters) {
      builder.addParameter(parameter.name ?: "", toJsonSchemaProperties(parameter))
    }
    return builder.build()
  }

  @OptIn(ExperimentalStdlibApi::class)
  private fun toJsonSchemaProperties(parameter: KParameter): Iterable<JsonSchemaProperty> {
    val type = parameter.type
    val annotation = parameter.findAnnotation<P>()
    val description = if (annotation == null) null else description(annotation.value)
    if (type == String::class.java) {
      return removeNulls(JsonSchemaProperty.STRING, description!!)
    }
    if (type == Boolean::class.javaPrimitiveType || type == Boolean::class.java) {
      return removeNulls(JsonSchemaProperty.BOOLEAN, description!!)
    }
    if (type == Byte::class.javaPrimitiveType || type == Byte::class.java || type == Short::class.javaPrimitiveType || type == Short::class.java || type == Int::class.javaPrimitiveType || type == Int::class.java || type == Long::class.javaPrimitiveType || type == Long::class.java || type == BigInteger::class.java) {
      return removeNulls(JsonSchemaProperty.INTEGER, description!!)
    }

    // TODO put constraints on min and max?
    if (type == Float::class.javaPrimitiveType || type == Float::class.java || type == Double::class.javaPrimitiveType || type == Double::class.java || type == BigDecimal::class.java) {
      return removeNulls(JsonSchemaProperty.NUMBER, description!!)
    }
    if (type == MutableList::class.java || type == MutableSet::class.java) { // TODO something else?
      return removeNulls(JsonSchemaProperty.ARRAY, description!!) // TODO provide type of array?
    }
    return if (type.isSubtypeOf(typeOf<Enum<*>>())) {
      removeNulls(JsonSchemaProperty.STRING, enums(*type.javaType.javaClass.getEnumConstants(), description!!))
    } else {
      removeNulls(JsonSchemaProperty.OBJECT, description!!)
    }
    // TODO provide internals
  }

  private fun removeNulls(vararg properties: JsonSchemaProperty): Iterable<JsonSchemaProperty> {
    return properties.toList()
  }
}
