package langchainkt.chain

interface Chain<Input, Output> {
  fun execute(input: Input): Output
}
