package langchainkt.model.language

import langchainkt.model.StreamingResponseHandler
import langchainkt.model.input.Prompt

/**
 * Represents a language model that has a simple text interface (as opposed to a chat interface)
 * and can stream a response one token at a time.
 * It is recommended to use the [StreamingChatLanguageModel] instead,
 * as it offers more features.
 */
interface StreamingLanguageModel {

  fun generate(prompt: String, handler: StreamingResponseHandler<String>)

  fun generate(prompt: Prompt, handler: StreamingResponseHandler<String>) {
    generate(prompt.text(), handler)
  }
}
