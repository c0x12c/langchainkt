package langchainkt.model.huggingface

data class TextGenerationRequest(
  val inputs: String? = null,
  val parameters: Parameters? = null,
  val options: Options? = null
)
