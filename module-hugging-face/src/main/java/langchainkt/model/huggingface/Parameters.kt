package langchainkt.model.huggingface

data class Parameters(
  val topK: Int? = null,
  val topP: Double? = null,
  val temperature: Double? = null,
  val repetitionPenalty: Double? = null,
  val maxNewTokens: Int? = null,
  val maxTime: Double? = null,
  val returnFullText: Boolean? = null,
  val numReturnSequences: Int? = null,
  val doSample: Boolean? = null
)
