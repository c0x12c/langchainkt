package langchainkt.model.huggingface

data class Options(
  val waitForModel: Boolean = true,
  val useCache: Boolean? = null
)
