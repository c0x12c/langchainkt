package langchainkt.model.input.structured

import com.google.gson.GsonBuilder
import com.google.gson.ToNumberPolicy
import com.google.gson.reflect.TypeToken
import kotlin.reflect.full.findAnnotation
import langchainkt.model.input.Prompt
import langchainkt.model.input.PromptTemplate

object StructuredPromptProcessor {

  private val gson = GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create()
  private var mapType: TypeToken<Map<String, Any>> = object : TypeToken<Map<String, Any>>() {}

  @JvmStatic
  fun toPrompt(structuredPrompt: Any): Prompt {
    val annotation = structuredPrompt::class.findAnnotation<StructuredPrompt>() ?: throw IllegalArgumentException(String.format(
      "%s should be annotated with @StructuredPrompt to be used as a structured prompt",
      structuredPrompt::class.simpleName
    ))
    val promptTemplateString = annotation.value.joinToString(annotation.delimiter)
    val promptTemplate = PromptTemplate.from(promptTemplateString)
    val variables = extractVariables(structuredPrompt)
    return promptTemplate.apply(variables)
  }

  private fun extractVariables(structuredPrompt: Any): Map<String, Any> {
    val json = gson.toJson(structuredPrompt)
    return gson.fromJson(json, mapType)
  }
}
