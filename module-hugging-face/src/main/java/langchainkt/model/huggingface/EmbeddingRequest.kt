package langchainkt.model.huggingface

data class EmbeddingRequest(
  val inputs: List<String>,
  val waitForModel: Boolean
) {
  val options: Options = Options(waitForModel)
}
