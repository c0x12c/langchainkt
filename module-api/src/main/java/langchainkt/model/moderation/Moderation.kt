package langchainkt.model.moderation

data class Moderation(
  private val flagged: Boolean = false,
  private val flaggedText: String? = null
) {

  fun flagged(): Boolean {
    return flagged
  }

  fun flaggedText(): String? {
    return flaggedText
  }

  companion object {
    fun flagged(flaggedText: String): Moderation {
      return Moderation(flaggedText = flaggedText, flagged = true)
    }

    fun notFlagged(): Moderation {
      return Moderation()
    }
  }
}
