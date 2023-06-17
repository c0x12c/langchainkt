package langchainkt.service

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.Proxy
import java.util.Collections
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.stream.Collectors
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.memberFunctions
import langchainkt.agent.tool.Tool
import langchainkt.agent.tool.ToolExecutionRequest
import langchainkt.agent.tool.ToolExecutor
import langchainkt.agent.tool.ToolSpecifications
import langchainkt.data.message.AiMessage
import langchainkt.data.message.ChatMessage
import langchainkt.data.message.ToolExecutionResultMessage
import langchainkt.data.segment.TextSegment
import langchainkt.exception.IllegalConfigurationException.Companion.illegalConfiguration
import langchainkt.memory.ChatMemory
import langchainkt.memory.chat.ChatMemoryProvider
import langchainkt.model.chat.ChatLanguageModel
import langchainkt.model.chat.StreamingChatLanguageModel
import langchainkt.model.input.PromptTemplate
import langchainkt.model.input.structured.StructuredPrompt
import langchainkt.model.input.structured.StructuredPromptProcessor
import langchainkt.model.moderation.Moderation
import langchainkt.model.moderation.ModerationModel
import langchainkt.retriever.Retriever
import langchainkt.service.ServiceOutputParser.outputFormatInstructions
import langchainkt.service.ServiceOutputParser.parse
import org.slf4j.LoggerFactory

/**
 * AI Services provide a simpler and more flexible alternative to chains.
 * You can define your own API (a Java interface with one or more methods),
 * and AiServices will provide an implementation for it (we call this "AI Service").
 *
 *
 * Currently, AI Services support:
 * <pre>
 * - Prompt templates for user and system messages using [UserMessage] and [SystemMessage]
 * - Structured prompts as method arguments (see [StructuredPrompt])
 * - Shared or per-user (see [MemoryId]) chat memory
 * - Retrievers
 * - Tools (see [Tool])
 * - Various return types (output parsers), see below
 * - Streaming (use [TokenStream] as a return type)
 * - Auto-moderation using [Moderate]
</pre> *
 *
 *
 * Here is the simplest example of an AI Service:
 *
 * <pre>
 * interface Assistant {
 *
 * String chat(String userMessage);
 * }
 *
 * Assistant assistant = AiServices.create(Assistant.class, model);
 *
 * String answer = assistant.chat("hello");
 * System.out.println(answer); // Hello, how can I help you today?
</pre> *
 *
 * <pre>
 * The return type of methods in your AI Service can be any of the following:
 * - a `String` or an [AiMessage], if you want to get the answer from the LLM as-is
 * - a `List<String>` or `Set<String>`, if you want to receive the answer as a collection of items or bullet points
 * - any `Enum` or a `boolean`, if you want to use the LLM for classification
 * - a primitive or boxed Java type: `int`, `Double`, etc., if you want to use the LLM for data extraction
 * - many default Java types: `Date`, `LocalDateTime`, `BigDecimal`, etc., if you want to use the LLM for data extraction
 * - any custom POJO, if you want to use the LLM for data extraction
</pre> *
 *
 *
 * Let's see how we can classify the sentiment of a text:
 * <pre>
 * enum Sentiment {
 * POSITIVE, NEUTRAL, NEGATIVE
 * }
 *
 * interface SentimentAnalyzer {
 *
 * `@UserMessage`("Analyze sentiment of {{it}}")
 * Sentiment analyzeSentimentOf(String text);
 * }
 *
 * SentimentAnalyzer assistant = AiServices.create(SentimentAnalyzer.class, model);
 *
 * Sentiment sentiment = analyzeSentimentOf.chat("I love you");
 * System.out.println(sentiment); // POSITIVE
</pre> *
 *
 *
 * As demonstrated, you can put [UserMessage] and [SystemMessage] annotations above a method to define
 * templates for user and system messages, respectively.
 * In this example, the special `{{it}}` prompt template variable is used because there's only one method parameter.
 * However, you can use more parameters as demonstrated in the following example:
 * <pre>
 * interface Translator {
 *
 * `@SystemMessage`("You are a professional translator into {{language}}")
 * `@UserMessage`("Translate the following text: {{text}}")
 * String translate(@V("text") String text, @V("language") String language);
 * }
</pre> *
 *
 *
 * See more examples [here](https://github.com/langchain4j/langchain4j-examples/tree/main/other-examples/src/main/java).
 *
 * @param <T> The interface for which AiServices will provide an implementation.
</T> */
class AiServices<T> private constructor(aiServiceClass: Class<T>?) {
  private val log = LoggerFactory.getLogger(AiServices::class.java)
  private val context = AiServiceContext()

  init {
    context.aiServiceClass = aiServiceClass
  }

  /**
   * Configures chat model that will be used under the hood of the AI Service.
   *
   *
   * Either [ChatLanguageModel] or [StreamingChatLanguageModel] should be configured,
   * but not both at the same time.
   *
   * @param chatLanguageModel Chat model that will be used under the hood of the AI Service.
   * @return builder
   */
  fun chatLanguageModel(chatLanguageModel: ChatLanguageModel?): AiServices<T> {
    context.chatModel = chatLanguageModel
    return this
  }
  // TODO separate retriever per user
  // TODO way to configure custom prompt with original message and context
  // TODO callback to transform/filter retrieved segments
  /**
   * Configures streaming chat model that will be used under the hood of the AI Service.
   * The methods of the AI Service must return a [TokenStream] type.
   *
   *
   * Either [ChatLanguageModel] or [StreamingChatLanguageModel] should be configured,
   * but not both at the same time.
   *
   * @param streamingChatLanguageModel Streaming chat model that will be used under the hood of the AI Service.
   * @return builder
   */
  fun streamingChatLanguageModel(streamingChatLanguageModel: StreamingChatLanguageModel): AiServices<T> {
    context.streamingChatModel = streamingChatLanguageModel
    return this
  }

  /**
   * Configures the chat memory that will be used to preserve conversation history between method calls.
   *
   *
   * Unless a [ChatMemory] or [ChatMemoryProvider] is configured, all method calls will be independent of each other.
   * In other words, the LLM will not remember the conversation from the previous method calls.
   *
   *
   * The same [ChatMemory] instance will be used for every method call.
   *
   *
   * If you want to have a separate [ChatMemory] for each user/conversation, configure [.chatMemoryProvider] instead.
   *
   *
   * Either a [ChatMemory] or a [ChatMemoryProvider] can be configured, but not both simultaneously.
   *
   * @param chatMemory An instance of chat memory to be used by the AI Service.
   * @return builder
   */
  fun chatMemory(chatMemory: ChatMemory): AiServices<T> {
    context.chatMemories = ConcurrentHashMap()
    context.chatMemories!![DEFAULT] = chatMemory
    return this
  }

  /**
   * Configures the chat memory provider, which provides a dedicated instance of [ChatMemory] for each user/conversation.
   * To distinguish between users/conversations, one of the method's arguments should be a memory ID (of any data type)
   * annotated with [MemoryId].
   * For each new (previously unseen) memoryId, an instance of [ChatMemory] will be automatically obtained
   * by invoking [ChatMemoryProvider.get].
   * Example:
   * <pre>
   * interface Assistant {
   *
   * String chat(@MemoryId int memoryId, @UserMessage String message);
   * }
  </pre> *
   * If you prefer to use the same (shared) [ChatMemory] for all users/conversations, configure a [.chatMemory] instead.
   *
   *
   * Either a [ChatMemory] or a [ChatMemoryProvider] can be configured, but not both simultaneously.
   *
   * @param chatMemoryProvider The provider of a [ChatMemory] for each new user/conversation.
   * @return builder
   */
  fun chatMemoryProvider(chatMemoryProvider: ChatMemoryProvider?): AiServices<T> {
    context.chatMemories = ConcurrentHashMap()
    context.chatMemoryProvider = chatMemoryProvider!!
    return this
  }

  /**
   * Configures a moderation model to be used for automatic content moderation.
   * If a method in the AI Service is annotated with [Moderate], the moderation model will be invoked
   * to check the user content for any inappropriate or harmful material.
   *
   * @param moderationModel The moderation model to be used for content moderation.
   * @return builder
   * @see Moderate
   */
  fun moderationModel(moderationModel: ModerationModel?): AiServices<T> {
    context.moderationModel = moderationModel
    return this
  }

  /**
   * Configures the tools that the LLM can use.
   * A [ChatMemory] that can hold at least 3 messages is required for the tools to work properly.
   *
   * @param objectsWithTools One or more objects whose methods are annotated with [Tool].
   * All these tools (methods annotated with [Tool]) will be accessible to the LLM.
   * Note that inherited methods are ignored.
   * @return builder
   * @see Tool
   */
  fun tools(vararg objectsWithTools: Any): AiServices<T> {
    return tools(listOf(*objectsWithTools))
  }

  /**
   * Configures the tools that the LLM can use.
   * A [ChatMemory] that can hold at least 3 messages is required for the tools to work properly.
   *
   * @param objectsWithTools A list of objects whose methods are annotated with [Tool].
   * All these tools (methods annotated with [Tool]) are accessible to the LLM.
   * Note that inherited methods are ignored.
   * @return builder
   * @see Tool
   */
  fun tools(objectsWithTools: List<Any>): AiServices<T> {
    context.toolSpecifications = ArrayList()
    context.toolExecutors = HashMap()
    for (obj in objectsWithTools) {
      for (method in obj::class.memberFunctions) {
        if (method.hasAnnotation<Tool>()) {
          val toolSpecification = ToolSpecifications.toolSpecificationFrom(method)
          context.toolSpecifications.add(toolSpecification)
          context.toolExecutors[toolSpecification.name()] = ToolExecutor(obj, method)
        }
      }
    }
    return this
  }

  /**
   * Configures a retriever that will be invoked on every method call to fetch relevant information
   * related to the current user message from an underlying source (e.g., embedding store).
   * This relevant information is automatically injected into the message sent to the LLM.
   *
   * @param retriever The retriever to be used by the AI Service.
   * @return builder
   */
  fun retriever(retriever: Retriever<TextSegment>?): AiServices<T> {
    context.retriever = retriever
    return this
  }

  /**
   * Constructs and returns the AI Service.
   *
   * @return An instance of the AI Service implementing the specified interface.
   */
  fun build(): T {
    if (context.chatModel == null && context.streamingChatModel == null) {
      throw illegalConfiguration("Please specify either chatLanguageModel or streamingChatLanguageModel")
    }
    for (method in context.aiServiceClass!!.getMethods()) {
      if (method.isAnnotationPresent(Moderate::class.java) && context.moderationModel == null) {
        throw illegalConfiguration("The @Moderate annotation is present, but the moderationModel is not set up. " +
          "Please ensure a valid moderationModel is configured before using the @Moderate annotation.")
      }
    }
    if (!context.hasChatMemory()) {
      throw illegalConfiguration(
        "Please set up chatMemory or chatMemoryProvider in order to use tools. "
          + "A ChatMemory that can hold at least 3 messages is required for the tools to work properly. "
          + "While the LLM can technically execute a tool without chat memory, if it only receives the " +
          "result of the tool's execution without the initial message from the user, it won't interpret " +
          "the result properly."
      )
    }
    val proxyInstance = Proxy.newProxyInstance(
      context.aiServiceClass!!.getClassLoader(), arrayOf(context.aiServiceClass),
      object : InvocationHandler {
        private val executor = Executors.newCachedThreadPool()

        @Throws(Exception::class)
        override fun invoke(proxy: Any, method: Method, args: Array<Any>): Any {
          if (method.declaringClass == Any::class.java) {
            // methods like equals(), hashCode() and toString() should not be handled by this proxy
            return method.invoke(this, *args)
          }
          validateParameters(method)
          val systemMessage = prepareSystemMessage(method, args)
          var userMessage = prepareUserMessage(method, args)
          if (context.retriever != null) { // TODO extract method/class
            val relevant = context.retriever!!.findRelevant(userMessage.text())
            if (relevant == null || relevant.isEmpty()) {
              log.debug("No relevant information was found")
            } else {
              val relevantConcatenated = relevant.stream()
                .map { obj: TextSegment -> obj.text() }
                .collect(Collectors.joining("\n\n"))
              log.debug("Retrieved relevant information:\n$relevantConcatenated\n")
              userMessage = langchainkt.data.message.UserMessage.userMessage("""
  ${userMessage.text()}
  
  Here is some information that might be useful for answering:
  
  $relevantConcatenated
  """.trimIndent())
            }
          }
          val memoryId = memoryId(method, args).orElse(DEFAULT)
          if (context.hasChatMemory()) {
            val chatMemory = context.chatMemory(memoryId)
            systemMessage.ifPresent { chatMemory!!.add(it) }
            chatMemory!!.add(userMessage)
          }
          val messages: MutableList<ChatMessage>
          if (context.hasChatMemory()) {
            messages = context.chatMemory(memoryId)!!.messages()
          } else {
            messages = ArrayList()
            systemMessage.ifPresent { e: ChatMessage -> messages.add(e) }
            messages.add(userMessage)
          }
          val moderationFuture = triggerModerationIfNeeded(method, messages)
          if (method.returnType == TokenStream::class.java) {
            return AiServiceTokenStream(messages, context, memoryId) // TODO moderation
          }
          var response = context.chatModel!!.generate(messages, context.toolSpecifications)
          verifyModerationIfNeeded(moderationFuture)
          var toolExecutionRequest: ToolExecutionRequest?
          while (true) { // TODO limit number of cycles
            if (context.hasChatMemory()) {
              context.chatMemory(memoryId)!!.add(response.content())
            }
            toolExecutionRequest = response.content().toolExecutionRequest()
            if (toolExecutionRequest == null) {
              break
            }
            val toolExecutor = context.toolExecutors[toolExecutionRequest.name()]
            val toolExecutionResult = toolExecutor!!.execute(toolExecutionRequest)
            val toolExecutionResultMessage = ToolExecutionResultMessage.toolExecutionResultMessage(toolExecutionRequest.name()!!, toolExecutionResult)
            val chatMemory = context.chatMemory(memoryId)
            chatMemory!!.add(toolExecutionResultMessage)
            response = context.chatModel!!.generate(chatMemory.messages(), context.toolSpecifications)
          }
          return parse(response, method.returnType)
        }

        private fun triggerModerationIfNeeded(method: Method, messages: List<ChatMessage>): Future<Moderation>? {
          return if (method.isAnnotationPresent(Moderate::class.java)) {
            executor.submit<Moderation> {
              val messagesToModerate = removeToolMessages(messages)
              context.moderationModel!!.moderate(messagesToModerate).content()
            }
          } else null
        }

        private fun removeToolMessages(messages: List<ChatMessage>): List<ChatMessage> {
          return messages.stream()
            .filter { it: ChatMessage? -> it !is ToolExecutionResultMessage }
            .filter { it: ChatMessage? -> !(it is AiMessage && it.toolExecutionRequest() != null) }
            .collect(Collectors.toList())
        }

        private fun verifyModerationIfNeeded(moderationFuture: Future<Moderation>?) {
          if (moderationFuture != null) {
            try {
              val moderation = moderationFuture.get()
              if (moderation.flagged()) {
                throw ModerationException(String.format("Text \"%s\" violates content policy", moderation.flaggedText()))
              }
            } catch (e: InterruptedException) {
              throw RuntimeException(e)
            } catch (e: ExecutionException) {
              throw RuntimeException(e)
            }
          }
        }
      })
    return proxyInstance as T
  }

  private fun memoryId(method: Method, args: Array<Any>): Optional<Any> {
    val parameters = method.parameters
    for (i in parameters.indices) {
      if (parameters[i].isAnnotationPresent(MemoryId::class.java)) {
        val memoryId = args[i]
        return Optional.of(memoryId)
      }
    }
    return Optional.empty()
  }

  private fun prepareSystemMessage(method: Method, args: Array<Any>): Optional<ChatMessage> {
    val parameters = method.parameters
    val variables = getPromptTemplateVariables(args, parameters)
    val annotation = method.getAnnotation(SystemMessage::class.java)
    if (annotation != null) {
      val systemMessageTemplate = java.lang.String.join(annotation.delimiter, *annotation.value)
      if (systemMessageTemplate.isEmpty()) {
        throw illegalConfiguration("@SystemMessage's template cannot be empty")
      }
      val prompt = PromptTemplate.from(systemMessageTemplate).apply(variables)
      return Optional.of(prompt.toSystemMessage())
    }
    return Optional.empty()
  }

  companion object {
    private const val DEFAULT = "default"

    /**
     * Creates an AI Service (an implementation of the provided interface), that is backed by the provided chat model.
     * This convenience method can be used to create simple AI Services.
     * For more complex cases, please use [.builder].
     *
     * @param aiService         The class of the interface to be implemented.
     * @param chatLanguageModel The chat model to be used under the hood.
     * @return An instance of the provided interface, implementing all its defined methods.
     */
    fun <T> create(aiService: Class<T>?, chatLanguageModel: ChatLanguageModel?): T {
      return builder(aiService)
        .chatLanguageModel(chatLanguageModel)
        .build()
    }

    /**
     * Creates an AI Service (an implementation of the provided interface), that is backed by the provided streaming chat model.
     * This convenience method can be used to create simple AI Services.
     * For more complex cases, please use [.builder].
     *
     * @param aiService                  The class of the interface to be implemented.
     * @param streamingChatLanguageModel The streaming chat model to be used under the hood.
     * The return type of all methods should be [TokenStream].
     * @return An instance of the provided interface, implementing all its defined methods.
     */
    fun <T> create(aiService: Class<T>, streamingChatLanguageModel: StreamingChatLanguageModel): T {
      return builder(aiService)
        .streamingChatLanguageModel(streamingChatLanguageModel)
        .build()
    }

    /**
     * Begins the construction of an AI Service.
     *
     * @param aiService The class of the interface to be implemented.
     * @return builder
     */
    @JvmStatic
    fun <T> builder(aiService: Class<T>?): AiServices<T> {
      return AiServices(aiService)
    }

    private fun prepareUserMessage(method: Method, args: Array<Any>?): ChatMessage {
      val parameters = method.parameters
      var variables: Map<String, Any> = getPromptTemplateVariables(args, parameters)
      val outputFormatInstructions = outputFormatInstructions(method.returnType)
      val userName = getUserName(parameters, args)
      val annotation = method.getAnnotation(UserMessage::class.java)
      if (annotation != null) {
        val userMessageTemplate = java.lang.String.join(annotation.delimiter, *annotation.value) + outputFormatInstructions
        if (userMessageTemplate.contains("{{it}}")) {
          if (parameters.size != 1) {
            throw illegalConfiguration("Error: The {{it}} placeholder is present but the method does not have exactly one parameter. " +
              "Please ensure that methods using the {{it}} placeholder have exactly one parameter.")
          }
          variables = Collections.singletonMap("it", toString(args!![0]))
        }
        val prompt = PromptTemplate.from(userMessageTemplate).apply(variables)
        return langchainkt.data.message.UserMessage.userMessage(userName, prompt.text())
      }
      for (i in parameters.indices) {
        if (parameters[i].isAnnotationPresent(UserMessage::class.java)) {
          return langchainkt.data.message.UserMessage.userMessage(userName, toString(args!![i]).toString() + outputFormatInstructions)
        }
      }
      if (args.isNullOrEmpty()) {
        throw illegalConfiguration("Method should have at least one argument")
      }
      if (args.size == 1) {
        return langchainkt.data.message.UserMessage.userMessage(userName, toString(args[0]).toString() + outputFormatInstructions)
      }
      throw illegalConfiguration("For methods with multiple parameters, each parameter must be annotated with @V, @UserMessage, @UserName or @MemoryId")
    }

    private fun getUserName(parameters: Array<Parameter>, args: Array<Any>?): String? {
      for (i in parameters.indices) {
        if (parameters[i].isAnnotationPresent(UserName::class.java)) {
          return args!![i].toString()
        }
      }
      return null
    }

    private fun validateParameters(method: Method) {
      val parameters = method.parameters
      if (parameters == null || parameters.size < 2) {
        return
      }
      for (parameter in parameters) {
        val v = parameter.getAnnotation(V::class.java)
        val userMessage = parameter.getAnnotation(UserMessage::class.java)
        val memoryId = parameter.getAnnotation(MemoryId::class.java)
        val userName = parameter.getAnnotation(UserName::class.java)
        if (v == null && userMessage == null && memoryId == null && userName == null) {
          throw illegalConfiguration(
            "Parameter '%s' of method '%s' should be annotated with @V or @UserMessage or @UserName or @MemoryId",
            parameter.name, method.name
          )
        }
      }
    }

    private fun getPromptTemplateVariables(args: Array<Any>?, parameters: Array<Parameter>): Map<String, Any> {
      val variables: MutableMap<String, Any> = HashMap()
      for (i in parameters.indices) {
        val varAnnotation = parameters[i].getAnnotation(V::class.java)
        if (varAnnotation != null) {
          val variableName = varAnnotation.value
          val variableValue = args!![i]
          variables[variableName] = variableValue
        }
      }
      return variables
    }

    private fun toString(arg: Any): Any {
      return if (arg.javaClass.isArray) {
        arrayToString(arg)
      } else if (arg.javaClass.isAnnotationPresent(StructuredPrompt::class.java)) {
        StructuredPromptProcessor.toPrompt(arg).text()
      } else {
        arg.toString()
      }
    }

    private fun arrayToString(arg: Any): String {
      val sb = StringBuilder("[")
      val length = java.lang.reflect.Array.getLength(arg)
      for (i in 0 until length) {
        sb.append(toString(java.lang.reflect.Array.get(arg, i)))
        if (i < length - 1) {
          sb.append(", ")
        }
      }
      sb.append("]")
      return sb.toString()
    }
  }
}
