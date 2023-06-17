package langchainkt.agent.tool

import java.util.Objects
import kotlin.reflect.KClass
import langchainkt.internal.Utils

class JsonSchemaProperty(
  private val key: String, private val value: Any
) {

  fun key(): String {
    return key
  }

  fun value(): Any {
    return value
  }

  override fun equals(other: Any?): Boolean {
    return if (this === other) true else (other is JsonSchemaProperty && equalTo(other))
  }

  private fun equalTo(another: JsonSchemaProperty): Boolean {
    return key == another.key && value == another.value
  }

  override fun hashCode(): Int {
    var h = 5381
    h += (h shl 5) + Objects.hashCode(key)
    h += (h shl 5) + Objects.hashCode(value)
    return h
  }

  override fun toString(): String {
    return ("JsonSchemaProperty {"
      + " key = " + Utils.quoted(key)
      + ", value = " + value
      + " }")
  }

  companion object {
    @JvmField
    val STRING = type("string")

    @JvmField
    val INTEGER = type("integer")

    @JvmField
    val NUMBER = type("number")

    @JvmField
    val OBJECT = type("object")

    @JvmField
    val ARRAY = type("array")

    @JvmField
    val BOOLEAN = type("boolean")

    val NULL = type("null")

    fun from(key: String, value: Any): JsonSchemaProperty {
      return JsonSchemaProperty(key, value)
    }

    fun property(key: String, value: Any): JsonSchemaProperty {
      return from(key, value)
    }

    fun type(value: String): JsonSchemaProperty {
      return from("type", value)
    }

    @JvmStatic
    fun description(value: String): JsonSchemaProperty {
      return from("description", value)
    }

    fun enums(vararg enumValues: String?): JsonSchemaProperty {
      return from("enum", enumValues)
    }

    @JvmStatic
    fun enums(vararg enumValues: Any): JsonSchemaProperty {
      for (enumValue in enumValues) {
        if (!enumValue.javaClass.isEnum) {
          throw RuntimeException("Value " + enumValue.javaClass.getName() + " should be enum")
        }
      }
      return from("enum", enumValues)
    }

    fun enums(enumClass: KClass<*>): JsonSchemaProperty {
      return from("enum", enumClass.java.enumConstants)
    }
  }
}
