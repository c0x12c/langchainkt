package langchainkt.internal

object Validators {

  @JvmStatic
  fun <T> ensureNotNull(`object`: T?, name: String?): T {
    if (`object` == null) {
      throw Exceptions.illegalArgument("%s cannot be null", name)
    }
    return `object`
  }

  fun <T : Collection<*>?> ensureNotEmpty(collection: T, name: String?): T {
    if (collection.isNullOrEmpty()) {
      throw Exceptions.illegalArgument("%s cannot be null or empty", name)
    }
    return collection
  }

  @JvmStatic
  fun ensureNotBlank(string: String?, name: String?): String {
    if (string == null || string.trim { it <= ' ' }.isEmpty()) {
      throw Exceptions.illegalArgument("%s cannot be null or blank", name)
    }
    return string
  }

  fun ensureTrue(expression: Boolean, msg: String?) {
    if (!expression) {
      throw Exceptions.illegalArgument(msg)
    }
  }

  fun ensureGreaterThanZero(i: Int?, name: String?): Int {
    if (i == null || i <= 0) {
      throw Exceptions.illegalArgument("%s must be greater than zero, but is: %s", name, i)
    }
    return i
  }

  fun ensureBetween(d: Double?, min: Double, max: Double, name: String?): Double {
    if (d == null || d < min || d > max) {
      throw Exceptions.illegalArgument("%s must be between %s and %s, but is: %s", name, min, max, d)
    }
    return d
  }

  fun ensureBetween(i: Int?, min: Int, max: Int, name: String?): Int {
    if (i == null || i < min || i > max) {
      throw Exceptions.illegalArgument("%s must be between %s and %s, but is: %s", name, min, max, i)
    }
    return i
  }
}
