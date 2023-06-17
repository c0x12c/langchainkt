package langchainkt.data.message

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule

val jackson = ObjectMapper().apply {
  propertyNamingStrategy = PropertyNamingStrategies.LOWER_CAMEL_CASE
  configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true)
  configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
  configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  setSerializationInclusion(JsonInclude.Include.NON_NULL)
  registerModule(
    KotlinModule.Builder()
      .withReflectionCacheSize(512)
      .configure(KotlinFeature.NullToEmptyCollection, false)
      .configure(KotlinFeature.NullToEmptyMap, false)
      .configure(KotlinFeature.NullIsSameAsDefault, false)
      .configure(KotlinFeature.SingletonSupport, false)
      .configure(KotlinFeature.StrictNullChecks, false)
      .build()
  )
}

object ChatMessageJson {

  fun serialize(message: ChatMessage): String {
    return jackson.writeValueAsString(message)
  }

  inline fun <reified T: ChatMessage> deserialize(json: String): T {
    return jackson.readValue(json, T::class.java)
  }
}
