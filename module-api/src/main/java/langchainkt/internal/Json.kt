package langchainkt.internal

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.stream.JsonWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Json {

  private val gson = GsonBuilder()
    .setPrettyPrinting()
    .registerTypeAdapter(
      LocalDate::class.java,
      JsonSerializer { localDate: LocalDate, _: Type?, _: JsonSerializationContext? -> JsonPrimitive(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)) } as JsonSerializer<LocalDate>
    )
    .registerTypeAdapter(
      LocalDate::class.java,
      JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? -> LocalDate.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE) } as JsonDeserializer<LocalDate>
    )
    .registerTypeAdapter(
      LocalDateTime::class.java,
      JsonSerializer { localDateTime: LocalDateTime, _: Type?, _: JsonSerializationContext? -> JsonPrimitive(localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) } as JsonSerializer<LocalDateTime>
    )
    .registerTypeAdapter(
      LocalDateTime::class.java,
      JsonDeserializer { json: JsonElement, _: Type?, _: JsonDeserializationContext? -> LocalDateTime.parse(json.asString, DateTimeFormatter.ISO_LOCAL_DATE_TIME) } as JsonDeserializer<LocalDateTime>
    )
    .create()

  @JvmStatic
  fun toJson(o: Any?): String {
    return gson.toJson(o)
  }

  @JvmStatic
  fun <T> fromJson(json: String?, type: Class<T>?): T {
    return gson.fromJson(json, type)
  }

  @JvmStatic
  @Throws(IOException::class)
  fun toInputStream(o: Any?, type: Class<*>?): InputStream {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
      OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8).use { outputStreamWriter ->
        JsonWriter(outputStreamWriter).use { jsonWriter ->
          gson.toJson(o, type, jsonWriter)
          jsonWriter.flush()
          return ByteArrayInputStream(byteArrayOutputStream.toByteArray())
        }
      }
    }
  }
}
