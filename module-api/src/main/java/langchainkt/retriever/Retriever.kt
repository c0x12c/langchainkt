package langchainkt.retriever

interface Retriever<T> {
  fun findRelevant(text: String): List<T>
}
