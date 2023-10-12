package langchainkt.service

import java.util.function.Consumer
import langchainkt.data.message.AiMessage
import langchainkt.data.message.ToolExecutionResultMessage
import langchainkt.model.StreamingResponseHandler
import langchainkt.model.output.Response
import langchainkt.model.output.TokenUsage
import org.slf4j.LoggerFactory

/**
 * Handles response from a language model for AI Service that is streamed token-by-token.
 * Handles both regular (text) responses and responses with the request to execute a tool.
 */
internal class AiServiceStreamingResponseHandler(
  private val context: AiServiceContext,
  private val memoryId: Any,
  private val tokenHandler: Consumer<String>,
  private val completionHandler: Consumer<Response<AiMessage>>?,
  private val errorHandler: Consumer<Throwable>?,
  private val tokenUsage: TokenUsage
) : StreamingResponseHandler<AiMessage> {

  private val log = LoggerFactory.getLogger(AiServiceStreamingResponseHandler::class.java)

  override fun onNext(token: String) {
    tokenHandler.accept(token)
  }

  override fun onComplete(response: Response<AiMessage>) {
    if (context.hasChatMemory()) {
      context.chatMemory(memoryId)!!.add(response.content())
    }
    val toolExecutionRequest = response.content().toolExecutionRequest()
    if (toolExecutionRequest != null) {
      val toolExecutor = context.toolExecutors[toolExecutionRequest.name()]
      val toolExecutionResult = toolExecutor!!.execute(toolExecutionRequest)
      val toolExecutionResultMessage = ToolExecutionResultMessage.from(
        toolExecutionRequest.name()!!,
        toolExecutionResult
      )
      context.chatMemory(memoryId)!!.add(toolExecutionResultMessage)
      context.streamingChatModel.generate(
        context.chatMemory(memoryId)!!.messages(),
        context.toolSpecifications,
        AiServiceStreamingResponseHandler(
          context,
          memoryId,
          tokenHandler,
          completionHandler,
          errorHandler,
          tokenUsage.add(response.tokenUsage()!!)
        )
      )
    } else {
      completionHandler?.accept(Response.from(
        response.content(),
        tokenUsage.add(response.tokenUsage()!!),
        response.finishReason())
      )
    }
  }

  override fun onError(error: Throwable?) {
    if (errorHandler != null) {
      try {
        error?.let {
          errorHandler.accept(error)
        }
      } catch (e: Exception) {
        log.error("While handling the following error...", error)
        log.error("...the following error happened", e)
      }
    } else {
      log.warn("Ignored error", error)
    }
  }
}
