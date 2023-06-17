package langchainkt.model.embedding

import langchainkt.data.embedding.Embedding
import langchainkt.data.segment.TextSegment
import langchainkt.model.output.Response

/**
 * Represents a model that can convert a given text into an embedding (vector representation of the text).
 */
interface EmbeddingModel {

  fun embed(text: String): Response<Embedding> {
    return embed(TextSegment.from(text))
  }

  fun embed(textSegment: TextSegment): Response<Embedding> {
    val response = embedAll(listOf(textSegment))
    return Response.from(
      response.content()[0],
      response.tokenUsage(),
      response.finishReason()
    )
  }

  fun embedAll(textSegments: List<TextSegment>): Response<List<Embedding>>
}
