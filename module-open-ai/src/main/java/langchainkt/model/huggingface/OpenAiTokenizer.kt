package langchainkt.model.huggingface

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import java.util.Optional
import java.util.function.Supplier
import langchainkt.agent.tool.ToolSpecification
import langchainkt.data.message.AiMessage
import langchainkt.data.message.ChatMessage
import langchainkt.data.message.ToolExecutionResultMessage
import langchainkt.data.message.UserMessage
import langchainkt.internal.Exceptions
import langchainkt.model.Tokenizer

class OpenAiTokenizer(private val modelName: String) : Tokenizer {

  // If the model is unknown, we should NOT fail fast during the creation of OpenAiTokenizer.
  // Doing so would cause the failure of every OpenAI***Model that uses this tokenizer.
  // This is done to account for situations when a new OpenAI model is available,
  // but JTokkit does not yet support it.
  private val encoding: Optional<Encoding> = Encodings.newLazyEncodingRegistry().getEncodingForModel(modelName)

  override fun estimateTokenCountInText(text: String): Int {
    return encoding.orElseThrow(unknownModelException())
      .countTokensOrdinary(text)
  }

  override fun estimateTokenCountInMessage(message: ChatMessage): Int {
    var tokenCount = 0
    tokenCount += extraTokensPerMessage()
    tokenCount += estimateTokenCountInText(message.text())
    tokenCount += estimateTokenCountInText(InternalOpenAiHelper.roleFrom(message).toString())
    if (message is UserMessage) {
      if (message.name() != null) {
        tokenCount += extraTokensPerName()
        tokenCount += estimateTokenCountInText(message.name()!!)
      }
    }
    if (message is AiMessage) {
      val toolExecutionRequest = message.toolExecutionRequest()
      if (toolExecutionRequest != null) {
        tokenCount += 4 // found experimentally while playing with OpenAI API
        tokenCount += estimateTokenCountInText(toolExecutionRequest.name()!!)
        tokenCount += estimateTokenCountInText(toolExecutionRequest.arguments()!!)
      }
    }
    if (message is ToolExecutionResultMessage) {
      tokenCount += -1 // found experimentally while playing with OpenAI API
      tokenCount += estimateTokenCountInText(message.toolName)
    }
    return tokenCount
  }

  override fun estimateTokenCountInMessages(messages: Iterable<ChatMessage>): Int {
    // see https://github.com/openai/openai-cookbook/blob/main/examples/How_to_count_tokens_with_tiktoken.ipynb
    var tokenCount = 3 // every reply is primed with <|start|>assistant<|message|>
    for (message in messages) {
      tokenCount += estimateTokenCountInMessage(message)
    }
    return tokenCount
  }

  override fun estimateTokenCountInToolSpecifications(toolSpecifications: Iterable<ToolSpecification>): Int {
    var tokenCount = 0
    for (toolSpecification in toolSpecifications) {
      tokenCount += estimateTokenCountInText(toolSpecification.name())
      tokenCount += estimateTokenCountInText(toolSpecification.description()!!)
      val properties: Map<String, Map<String, Any>> = toolSpecification.parameters()!!.properties()
      for (property in properties.keys) {
        for ((key, value) in properties[property]!!) {
          when (key) {
            "type" -> {
              tokenCount += 3 // found experimentally while playing with OpenAI API
              tokenCount += estimateTokenCountInText(value.toString())
            }

            "description" -> {
              tokenCount += 3 // found experimentally while playing with OpenAI API
              tokenCount += estimateTokenCountInText(value.toString())
            }

            "enum" -> {
              tokenCount -= 3 // found experimentally while playing with OpenAI API
              for (enumValue in value as Array<*>) {
                tokenCount += 3 // found experimentally while playing with OpenAI API
                tokenCount += estimateTokenCountInText(enumValue.toString())
              }
            }
          }
        }
      }
      tokenCount += 12 // found experimentally while playing with OpenAI API
    }
    tokenCount += 12 // found experimentally while playing with OpenAI API
    return tokenCount
  }

  private fun extraTokensPerMessage(): Int {
    return if (modelName == OpenAiModelName.GPT_3_5_TURBO_0301) {
      4
    } else {
      3
    }
  }

  private fun extraTokensPerName(): Int {
    return if (modelName == OpenAiModelName.GPT_3_5_TURBO_0301) {
      -1 // if there's a name, the role is omitted
    } else {
      1
    }
  }

  fun encode(text: String): List<Int> {
    return encoding
      .orElseThrow(unknownModelException())
      .encodeOrdinary(text)
  }

  fun encode(text: String, maxTokensToEncode: Int): List<Int> {
    return encoding
      .orElseThrow(unknownModelException())
      .encodeOrdinary(text, maxTokensToEncode).tokens
  }

  fun decode(tokens: List<Int>): String {
    return encoding
      .orElseThrow(unknownModelException())
      .decode(tokens)
  }

  private fun unknownModelException(): Supplier<IllegalArgumentException> {
    return Supplier { Exceptions.illegalArgument("Model '%s' is unknown to jtokkit", modelName) }
  }
}
