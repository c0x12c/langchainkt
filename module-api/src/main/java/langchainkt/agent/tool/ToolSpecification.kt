package langchainkt.agent.tool

import java.util.Objects
import langchainkt.internal.Utils

class ToolSpecification private constructor(builder: Builder) {
  private val name: String
  private val description: String?
  private val parameters: ToolParameters?

  init {
    name = builder.name
    description = builder.description
    parameters = builder.parameters
  }

  fun name(): String {
    return name
  }

  fun description(): String? {
    return description
  }

  fun parameters(): ToolParameters? {
    return parameters
  }

  override fun equals(another: Any?): Boolean {
    return if (this === another) true else (another is ToolSpecification
      && equalTo(another))
  }

  private fun equalTo(another: ToolSpecification): Boolean {
    return name == another.name && description == another.description && parameters == another.parameters
  }

  override fun hashCode(): Int {
    var h = 5381
    h += (h shl 5) + Objects.hashCode(name)
    h += (h shl 5) + Objects.hashCode(description)
    h += (h shl 5) + Objects.hashCode(parameters)
    return h
  }

  override fun toString(): String {
    return ("ToolSpecification {"
      + " name = " + Utils.quoted(name)
      + ", description = " + Utils.quoted(description)
      + ", parameters = " + parameters
      + " }")
  }

  class Builder {
    internal lateinit var name: String
    internal var description: String? = null
    internal var parameters: ToolParameters? = null

    fun name(name: String): Builder {
      this.name = name
      return this
    }

    fun description(description: String?): Builder {
      this.description = description
      return this
    }

    fun parameters(parameters: ToolParameters): Builder {
      this.parameters = parameters
      return this
    }

    fun addParameter(name: String, vararg jsonSchemaProperties: JsonSchemaProperty): Builder {
      return addParameter(name, listOf(*jsonSchemaProperties))
    }

    fun addParameter(name: String, jsonSchemaProperties: Iterable<JsonSchemaProperty>): Builder {
      addOptionalParameter(name, jsonSchemaProperties)
      parameters?.required()?.add(name)
      return this
    }

    fun addOptionalParameter(name: String, vararg jsonSchemaProperties: JsonSchemaProperty): Builder {
      return addOptionalParameter(name, listOf(*jsonSchemaProperties))
    }

    fun addOptionalParameter(name: String, jsonSchemaProperties: Iterable<JsonSchemaProperty>): Builder {
      if (parameters == null) {
        parameters = ToolParameters.builder().build()
      }
      val jsonSchemaPropertiesMap: MutableMap<String, Any> = HashMap()
      for (jsonSchemaProperty in jsonSchemaProperties) {
        jsonSchemaPropertiesMap[jsonSchemaProperty.key()] = jsonSchemaProperty.value()
      }
      parameters!!.properties()[name] = jsonSchemaPropertiesMap
      return this
    }

    fun build(): ToolSpecification {
      return ToolSpecification(this)
    }
  }

  companion object {
    @JvmStatic
    fun builder(): Builder {
      return Builder()
    }
  }
}
