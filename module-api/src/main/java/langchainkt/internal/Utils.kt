package langchainkt.internal

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.UUID

object Utils {
  fun <T> getOrDefault(value: T?, defaultValue: T): T {
    return value ?: defaultValue
  }

  fun isNullOrBlank(string: String?): Boolean {
    return string == null || string.trim { it <= ' ' }.isEmpty()
  }

  @JvmStatic
  fun randomUUID(): String {
    return UUID.randomUUID().toString()
  }

  @JvmStatic
  fun generateUUIDFrom(input: String): String {
    return try {
      val hashBytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(StandardCharsets.UTF_8))
      val sb = StringBuilder()
      for (b in hashBytes) sb.append(String.format("%02x", b))
      UUID.nameUUIDFromBytes(sb.toString().toByteArray(StandardCharsets.UTF_8)).toString()
    } catch (e: NoSuchAlgorithmException) {
      throw IllegalArgumentException(e)
    }
  }

  @JvmStatic
  fun quoted(string: String?): String {
    return if (string == null) {
      "null"
    } else {
      "\"" + string + "\""
    }
  }

  fun firstChars(string: String?, numberOfChars: Int): String? {
    if (string == null) {
      return null
    }
    return if (string.length > numberOfChars) string.substring(0, numberOfChars) else string
  }
}
