package langchainkt.chain

import langchainkt.data.segment.TextSegment
import langchainkt.internal.Validators
import langchainkt.memory.ChatMemory
import langchainkt.memory.chat.MessageWindowChatMemory
import langchainkt.model.chat.ChatLanguageModel
import langchainkt.model.input.PromptTemplate
import langchainkt.retriever.Retriever

/**
 * A chain for interacting with a specified [ChatLanguageModel] based on the information provided by a specified [Retriever].
 * Includes a default [PromptTemplate], which can be overridden.
 * Includes a default [ChatMemory] (a message window with maximum 10 messages), which can be overridden.
 */
class ConversationalRetrievalChain(
  private val chatLanguageModel: ChatLanguageModel,
  private val chatMemory: ChatMemory = MessageWindowChatMemory.withMaxMessages(10),
  private val promptTemplate: PromptTemplate = DEFAULT_PROMPT_TEMPLATE,
  private val retriever: Retriever<TextSegment>
) : Chain<String, String> {

  override fun execute(input: String): String {
    var question = input
    question = Validators.ensureNotBlank(question, "question")
    val relevantSegments = retriever.findRelevant(question)
    val variables: MutableMap<String, Any> = HashMap()
    variables["question"] = question
    variables["information"] = format(relevantSegments)
    val userMessage = promptTemplate.apply(variables).toUserMessage()
    chatMemory.add(userMessage)
    val aiMessage = chatLanguageModel.generate(chatMemory.messages()).content()
    chatMemory.add(aiMessage)
    return aiMessage.text()!!
  }

  companion object {
    private val DEFAULT_PROMPT_TEMPLATE = PromptTemplate.from(
      """
            Answer the following question to the best of your ability: {{question}}
            
            Base your answer on the following information:
            {{information}}
            """.trimIndent())

    private fun format(relevantSegments: List<TextSegment>): String {
      return relevantSegments
        .map { it.text() }
        .joinToString("\n\n") { segment: String ->
          "...$segment..."
        }
    }
  }
}
