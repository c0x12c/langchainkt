package langchainkt.service

import java.util.function.Consumer

interface OnError {
  /**
   * The provided Consumer will be invoked when an error occurs during streaming.
   *
   * @param errorHandler lambda that will be invoked when an error occurs
   * @return the next step of the step-builder
   */
  fun onError(errorHandler: Consumer<Throwable>): OnStart

  /**
   * All errors during streaming will be ignored (but will be logged with a WARN log level).
   *
   * @return the next step of the step-builder
   */
  fun ignoreErrors(): OnStart
}
