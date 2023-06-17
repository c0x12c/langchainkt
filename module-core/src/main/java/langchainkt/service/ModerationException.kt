package langchainkt.service

/**
 * Thrown when content moderation fails, i.e., when content is flagged by the moderation model.
 *
 * @see Moderate
 *
 * @see langchainkt.model.moderation.ModerationModel
 */
class ModerationException(
  message: String?
) : RuntimeException(message)
