package langchainkt.service

import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Date
import langchainkt.data.message.AiMessage
import langchainkt.exception.IllegalConfigurationException.Companion.illegalConfiguration
import langchainkt.internal.Json
import langchainkt.model.output.BigDecimalOutputParser
import langchainkt.model.output.BigIntegerOutputParser
import langchainkt.model.output.BooleanOutputParser
import langchainkt.model.output.ByteOutputParser
import langchainkt.model.output.DateOutputParser
import langchainkt.model.output.DoubleOutputParser
import langchainkt.model.output.EnumOutputParser
import langchainkt.model.output.FloatOutputParser
import langchainkt.model.output.IntOutputParser
import langchainkt.model.output.LocalDateOutputParser
import langchainkt.model.output.LocalDateTimeOutputParser
import langchainkt.model.output.LocalTimeOutputParser
import langchainkt.model.output.LongOutputParser
import langchainkt.model.output.OutputParser
import langchainkt.model.output.Response
import langchainkt.model.output.ShortOutputParser
import langchainkt.model.output.structured.Description

internal object ServiceOutputParser {
  private val OUTPUT_PARSERS: MutableMap<Class<*>?, OutputParser<*>> = HashMap()

  init {
    OUTPUT_PARSERS[Boolean::class.javaPrimitiveType] = BooleanOutputParser()
    OUTPUT_PARSERS[Boolean::class.java] = BooleanOutputParser()
    OUTPUT_PARSERS[Byte::class.javaPrimitiveType] = ByteOutputParser()
    OUTPUT_PARSERS[Byte::class.java] = ByteOutputParser()
    OUTPUT_PARSERS[Short::class.javaPrimitiveType] = ShortOutputParser()
    OUTPUT_PARSERS[Short::class.java] = ShortOutputParser()
    OUTPUT_PARSERS[Int::class.javaPrimitiveType] = IntOutputParser()
    OUTPUT_PARSERS[Int::class.java] = IntOutputParser()
    OUTPUT_PARSERS[Long::class.javaPrimitiveType] = LongOutputParser()
    OUTPUT_PARSERS[Long::class.java] = LongOutputParser()
    OUTPUT_PARSERS[BigInteger::class.java] = BigIntegerOutputParser()
    OUTPUT_PARSERS[Float::class.javaPrimitiveType] = FloatOutputParser()
    OUTPUT_PARSERS[Float::class.java] = FloatOutputParser()
    OUTPUT_PARSERS[Double::class.javaPrimitiveType] = DoubleOutputParser()
    OUTPUT_PARSERS[Double::class.java] = DoubleOutputParser()
    OUTPUT_PARSERS[BigDecimal::class.java] = BigDecimalOutputParser()
    OUTPUT_PARSERS[Date::class.java] = DateOutputParser()
    OUTPUT_PARSERS[LocalDate::class.java] = LocalDateOutputParser()
    OUTPUT_PARSERS[LocalTime::class.java] = LocalTimeOutputParser()
    OUTPUT_PARSERS[LocalDateTime::class.java] = LocalDateTimeOutputParser()
  }

  @JvmStatic
  fun parse(response: Response<AiMessage>, returnType: Class<*>): Any {
    if (returnType == Response::class.java) {
      return response
    }
    val aiMessage = response.content()
    if (returnType == AiMessage::class.java) {
      return aiMessage
    }
    val text = aiMessage.text()
    if (returnType == String::class.java) {
      return text
    }
    val outputParser = OUTPUT_PARSERS[returnType]
    if (outputParser != null) {
      return outputParser.parse(text)!!
    }
    if (returnType == MutableList::class.java) {
      return listOf(*text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
    }
    return if (returnType == MutableSet::class.java) {
      HashSet(listOf(*text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()))
    } else Json.fromJson(text, returnType)
  }

  @JvmStatic
  fun outputFormatInstructions(returnType: Class<*>): String {
    if (returnType == String::class.java || returnType == AiMessage::class.java || returnType == TokenStream::class.java || returnType == Response::class.java) {
      return ""
    }
    if (returnType == Void.TYPE) {
      throw illegalConfiguration("Return type of method '%s' cannot be void")
    }
    if (returnType.isEnum) {
      val formatInstructions = EnumOutputParser(returnType.asSubclass(Enum::class.java)).formatInstructions()
      return "\nYou must answer strictly in the following format: $formatInstructions"
    }
    val outputParser = OUTPUT_PARSERS[returnType]
    if (outputParser != null) {
      val formatInstructions = outputParser.formatInstructions()
      return "\nYou must answer strictly in the following format: $formatInstructions"
    }
    return if (returnType == MutableList::class.java || returnType == MutableSet::class.java) {
      "\nYou must put every item on a separate line."
    } else """
   
   You must answer strictly in the following JSON format: ${jsonStructure(returnType)}
   """.trimIndent()
  }

  private fun jsonStructure(structured: Class<*>): String {
    val jsonSchema = StringBuilder()
    jsonSchema.append("{\n")
    for (field in structured.getDeclaredFields()) {
      jsonSchema.append(String.format("\"%s\": (%s),\n", field.name, descriptionFor(field)))
    }
    jsonSchema.append("}")
    return jsonSchema.toString()
  }

  private fun descriptionFor(field: Field): String {
    val fieldDescription = field.getAnnotation(Description::class.java)
      ?: return "type: " + typeOf(field)
    return java.lang.String.join(" ", *fieldDescription.value) + "; type: " + typeOf(field)
  }

  private fun typeOf(field: Field): String {
    val type = field.genericType
    if (type is ParameterizedType) {
      val typeArguments = type.actualTypeArguments
      if (type.rawType == MutableList::class.java || type.rawType == MutableSet::class.java) {
        return String.format("array of %s", simpleTypeName(typeArguments[0]))
      }
    } else if (field.type.isArray) {
      return String.format("array of %s", simpleTypeName(field.type.componentType))
    } else if ((type as Class<*>).isEnum) {
      return "enum, must be one of " + type.getEnumConstants().contentToString()
    }
    return simpleTypeName(type)
  }

  private fun simpleTypeName(type: Type): String {
    return when (type.typeName) {
      "java.lang.String" -> "string"
      "java.lang.Integer", "int" -> "integer"
      "java.lang.Boolean", "boolean" -> "boolean"
      "java.lang.Float", "float" -> "float"
      "java.lang.Double", "double" -> "double"
      "java.util.Date", "java.time.LocalDate" -> "date string (2023-12-31)"
      "java.time.LocalTime" -> "time string (23:59:59)"
      "java.time.LocalDateTime" -> "date-time string (2023-12-31T23:59:59)"
      else -> type.typeName
    }
  }
}
