package langchainkt.classification

import langchainkt.data.document.Document
import langchainkt.data.segment.TextSegment

/**
 * Classifies given text according to specified enum.
 *
 * @param <E> Enum that is the result of classification.
</E> */
interface TextClassifier<E : Enum<E>> {
  /**
   * Classify the given text.
   *
   * @param text Text to classify.
   * @return A list of classification categories.
   */
  fun classify(text: String): List<E>

  /**
   * Classify the given [TextSegment].
   *
   * @param segment [TextSegment] to classify.
   * @return A list of classification categories.
   */
  fun classify(segment: TextSegment): List<E> {
    return classify(segment.text())
  }

  /**
   * Classify the given [Document].
   *
   * @param document [Document] to classify.
   * @return A list of classification categories.
   */
  fun classify(document: Document): List<E> {
    return classify(document.text())
  }
}
