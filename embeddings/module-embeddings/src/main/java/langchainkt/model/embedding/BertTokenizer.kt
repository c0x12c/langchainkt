package langchainkt.model.embedding

import ai.djl.modality.nlp.DefaultVocabulary
import ai.djl.modality.nlp.Vocabulary
import ai.djl.modality.nlp.bert.BertFullTokenizer
import java.net.URL
import langchainkt.agent.tool.ToolSpecification
import langchainkt.data.message.ChatMessage
import langchainkt.model.Tokenizer

class BertTokenizer : Tokenizer {
  private val tokenizer: BertFullTokenizer

  constructor() {
    tokenizer = createTokenizerFrom(javaClass.getResource("/bert-vocabulary-en.txt")!!)
  }

  constructor(vocabularyFile: URL) {
    tokenizer = createTokenizerFrom(vocabularyFile)
  }

  override fun estimateTokenCountInText(text: String): Int {
    return tokenizer.tokenize(text).size
  }

  override fun estimateTokenCountInMessage(message: ChatMessage): Int {
    return estimateTokenCountInText(message.text())
  }

  override fun estimateTokenCountInMessages(messages: Iterable<ChatMessage>): Int {
    var tokens = 0
    for (message in messages) {
      tokens += estimateTokenCountInMessage(message)
    }
    return tokens
  }

  override fun estimateTokenCountInToolSpecifications(toolSpecifications: Iterable<ToolSpecification>): Int {
    throw RuntimeException("Not implemented yet")
  }

  fun tokenize(text: String?): List<String> {
    return tokenizer.tokenize(text)
  }

  fun tokenId(token: String?): Long {
    return tokenizer.vocabulary.getIndex(token)
  }

  companion object {
    private fun createTokenizerFrom(vocabularyFile: URL): BertFullTokenizer {
      return try {
        val vocabulary: Vocabulary = DefaultVocabulary.builder()
          .addFromTextFile(vocabularyFile)
          .build()
        BertFullTokenizer(vocabulary, true)
      } catch (e: Exception) {
        throw RuntimeException(e)
      }
    }
  }
}
