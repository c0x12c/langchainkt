package langchainkt.data.embedding

/**
 * Represents a dense vector embedding of a text.
 * This class encapsulates a float array that captures the "meaning" or semantic information of the text.
 * Texts with similar meanings will have their vectors located close to each other in the embedding space.
 * The embeddings are typically created by embedding models.
 *
 * @see EmbeddingModel
 */
class Embedding(
  private val vector: FloatArray
) {

  fun vector(): FloatArray {
    return vector
  }

  fun vectorAsList(): List<Float> {
    val list: MutableList<Float> = ArrayList(vector.size)
    for (f in vector) {
      list.add(f)
    }
    return list
  }

  fun dimensions(): Int {
    return vector.size
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val that = other as Embedding
    return vector.contentEquals(that.vector)
  }

  override fun hashCode(): Int {
    return vector.contentHashCode()
  }

  override fun toString(): String {
    return "Embedding {" +
      " vector = " + vector.contentToString() +
      " }"
  }

  companion object {
    @JvmStatic
    fun from(vector: FloatArray): Embedding {
      return Embedding(vector)
    }

    @JvmStatic
    fun from(vector: List<Float>): Embedding {
      val array = FloatArray(vector.size)
      for (i in vector.indices) {
        array[i] = vector[i]
      }
      return Embedding(array)
    }
  }
}
