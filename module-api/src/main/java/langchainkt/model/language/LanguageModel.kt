package langchainkt.model.language

import langchainkt.model.input.Prompt
import langchainkt.model.output.Response

/**
 * Represents a language model that has a simple text interface (as opposed to a chat interface).
 * It is recommended to use the [ChatLanguageModel] instead,
 * as it offers more features.
 */
interface LanguageModel {
  fun generate(prompt: String): Response<String>

  fun generate(prompt: Prompt): Response<String> {
    return generate(prompt.text())
  }
}
