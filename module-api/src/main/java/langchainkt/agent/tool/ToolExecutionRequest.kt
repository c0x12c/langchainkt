package langchainkt.agent.tool

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Objects
import langchainkt.internal.Utils

class ToolExecutionRequest private constructor(
  builder: Builder
) {
  private val name: String?
  private val arguments: String?

  init {
    name = builder.name
    arguments = builder.arguments
  }

  fun name(): String? {
    return name
  }

  fun arguments(): String? {
    return arguments
  }

  fun argumentsAsMap(): Map<String, Any> {
    return GSON.fromJson(arguments, MAP_TYPE)
  }

  fun <T> argument(name: String): T? {
    val arguments = argumentsAsMap()
    return arguments[name] as T?
  }

  override fun equals(other: Any?): Boolean {
    return if (this === other) true else (other is ToolExecutionRequest && equalTo(other))
  }

  private fun equalTo(another: ToolExecutionRequest): Boolean {
    return name == another.name && arguments == another.arguments
  }

  override fun hashCode(): Int {
    var h = 5381
    h += (h shl 5) + Objects.hashCode(name)
    h += (h shl 5) + Objects.hashCode(arguments)
    return h
  }

  override fun toString(): String {
    return ("ToolExecutionRequest {"
      + " name = " + Utils.quoted(name)
      + ", arguments = " + Utils.quoted(arguments)
      + " }")
  }

  class Builder internal constructor() {
    internal var name: String? = null
    internal var arguments: String? = null
    fun name(name: String?): Builder {
      this.name = name
      return this
    }

    fun arguments(arguments: String?): Builder {
      this.arguments = arguments
      return this
    }

    fun build(): ToolExecutionRequest {
      return ToolExecutionRequest(this)
    }
  }

  companion object {
    private val GSON = Gson()
    private val MAP_TYPE = object : TypeToken<Map<String, Any>>() {}.type

    @JvmStatic
    fun builder(): Builder {
      return Builder()
    }
  }
}
