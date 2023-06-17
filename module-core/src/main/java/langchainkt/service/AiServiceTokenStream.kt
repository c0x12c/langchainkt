package langchainkt.service

import java.util.function.Consumer
import langchainkt.data.message.AiMessage
import langchainkt.data.message.ChatMessage
import langchainkt.internal.Validators
import langchainkt.model.output.Response
import langchainkt.model.output.TokenUsage

internal class AiServiceTokenStream(
  private val messagesToSend: List<ChatMessage>,
  private val context: AiServiceContext,
  private val memoryId: Any
) : TokenStream {

  init {
    Validators.ensureNotEmpty(messagesToSend, "messagesToSend")
    Validators.ensureNotNull(context.streamingChatModel, "streamingChatModel")
  }

  override fun onNext(tokenHandler: Consumer<String>): OnCompleteOrOnError {
    return object : OnCompleteOrOnError {
      override fun onComplete(completionHandler: Consumer<Response<AiMessage>>): OnError {
        return object : OnError {
          override fun onError(errorHandler: Consumer<Throwable>): OnStart {
            return AiServiceOnStart(tokenHandler, completionHandler, errorHandler)
          }

          override fun ignoreErrors(): OnStart {
            return AiServiceOnStart(tokenHandler, completionHandler, null)
          }
        }
      }

      override fun onError(errorHandler: Consumer<Throwable>): OnStart {
        return AiServiceOnStart(tokenHandler, null, errorHandler)
      }

      override fun ignoreErrors(): OnStart {
        return AiServiceOnStart(tokenHandler, null, null)
      }
    }
  }

  private inner class AiServiceOnStart(
    private val tokenHandler: Consumer<String>,
    private val completionHandler: Consumer<Response<AiMessage>>?,
    private val errorHandler: Consumer<Throwable>?
  ) : OnStart {

    override fun start() {
      context.streamingChatModel.generate(
        messagesToSend,
        context.toolSpecifications,
        AiServiceStreamingResponseHandler(
          context,
          memoryId,
          tokenHandler,
          completionHandler,
          errorHandler,
          TokenUsage()
        )
      )
    }
  }
}
