package langchainkt.agent.tool

import java.util.Objects
import langchainkt.internal.Utils

class ToolParameters private constructor(builder: Builder) {
  private val type: String
  private val properties: MutableMap<String, Map<String, Any>>
  private val required: MutableList<String>

  init {
    type = builder.type
    properties = builder.properties
    required = builder.required
  }

  fun type(): String {
    return type
  }

  fun properties(): MutableMap<String, Map<String, Any>> {
    return properties
  }

  fun required(): MutableList<String> {
    return required
  }

  override fun equals(other: Any?): Boolean {
    return if (this === other) true else (other is ToolParameters && equalTo(other))
  }

  private fun equalTo(another: ToolParameters): Boolean {
    return type == another.type && properties == another.properties && required == another.required
  }

  override fun hashCode(): Int {
    var h = 5381
    h += (h shl 5) + Objects.hashCode(type)
    h += (h shl 5) + Objects.hashCode(properties)
    h += (h shl 5) + Objects.hashCode(required)
    return h
  }

  override fun toString(): String {
    return ("ToolParameters {"
      + " type = " + Utils.quoted(type)
      + ", properties = " + properties
      + ", required = " + required
      + " }")
  }

  class Builder internal constructor() {
    internal var type = "object"
    internal var properties: MutableMap<String, Map<String, Any>> = HashMap()
    internal var required: MutableList<String> = ArrayList()

    fun type(type: String): Builder {
      this.type = type
      return this
    }

    fun properties(properties: MutableMap<String, Map<String, Any>>): Builder {
      this.properties = properties
      return this
    }

    fun required(required: MutableList<String>): Builder {
      this.required = required
      return this
    }

    fun build(): ToolParameters {
      return ToolParameters(this)
    }
  }

  companion object {
    @JvmStatic
    fun builder(): Builder {
      return Builder()
    }
  }
}
