package langchainkt.model.embedding

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtException
import ai.onnxruntime.OrtSession
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.LongBuffer
import java.util.stream.Collectors
import kotlin.math.min
import kotlin.math.sqrt

class OnnxBertBiEncoder(
  modelInputStream: InputStream,
  vocabularyFile: URL,
  private val poolingMode: PoolingMode
) {
  private val environment: OrtEnvironment
  private val session: OrtSession
  private val tokenizer: BertTokenizer

  init {
    try {
      environment = OrtEnvironment.getEnvironment()
      session = environment.createSession(loadModel(modelInputStream))
      tokenizer = BertTokenizer(vocabularyFile)
    } catch (e: Exception) {
      throw RuntimeException(e)
    }
  }

  fun embed(text: String): FloatArray {
    val wordPieces = tokenizer.tokenize(text)
    val partitions = partition(wordPieces, MAX_SEQUENCE_LENGTH)
    val embeddings: MutableList<FloatArray> = ArrayList()
    for (partition in partitions) {
      val tokens = toTokens(partition)
      try {
        encode(tokens).use { result ->
          val embedding = toEmbedding(result)
          embeddings.add(embedding)
        }
      } catch (e: OrtException) {
        throw RuntimeException(e)
      }
    }
    val weights = partitions.stream()
      .map { obj: List<String> -> obj.size }
      .collect(Collectors.toList())
    return normalize(weightedAverage(embeddings, weights))
  }

  private fun toTokens(wordPieces: List<String>): LongArray {
    val tokens = LongArray(wordPieces.size + 2)
    var i = 0
    tokens[i++] = tokenizer.tokenId(CLS)
    for (wordPiece in wordPieces) {
      tokens[i++] = tokenizer.tokenId(wordPiece)
    }
    tokens[i] = tokenizer.tokenId(SEP)
    return tokens
  }

  @Throws(OrtException::class)
  private fun encode(tokens: LongArray): OrtSession.Result {
    val attentionMasks = LongArray(tokens.size)
    for (i in tokens.indices) {
      attentionMasks[i] = 1L
    }
    val tokenTypeIds = LongArray(tokens.size)
    for (i in tokens.indices) {
      tokenTypeIds[i] = 0L
    }
    val shape = longArrayOf(1, tokens.size.toLong())
    OnnxTensor.createTensor(environment, LongBuffer.wrap(tokens), shape).use { tokensTensor ->
      OnnxTensor.createTensor(environment, LongBuffer.wrap(attentionMasks), shape).use { attentionMasksTensor ->
        OnnxTensor.createTensor(environment, LongBuffer.wrap(tokenTypeIds), shape).use { tokenTypeIdsTensor ->
          val inputs: MutableMap<String, OnnxTensor> = HashMap()
          inputs["input_ids"] = tokensTensor
          inputs["token_type_ids"] = tokenTypeIdsTensor
          inputs["attention_mask"] = attentionMasksTensor
          return session.run(inputs)
        }
      }
    }
  }

  @Throws(OrtException::class)
  private fun toEmbedding(result: OrtSession.Result): FloatArray {
    @Suppress("UNCHECKED_CAST")
    val vectors = (result[0].value as Array<Array<FloatArray>>)[0]
    return pool(vectors)
  }

  private fun pool(vectors: Array<FloatArray>): FloatArray {
    return when (poolingMode) {
      PoolingMode.CLS -> clsPool(vectors)
      PoolingMode.MEAN -> meanPool(vectors)
    }
  }

  private fun weightedAverage(embeddings: List<FloatArray>, weights: List<Int>): FloatArray {
    if (embeddings.size == 1) {
      return embeddings[0]
    }
    val dimensions = embeddings[0].size
    val averagedEmbedding = FloatArray(dimensions)
    var totalWeight = 0
    for (i in embeddings.indices) {
      val weight = weights[i]
      totalWeight += weight
      for (j in 0 until dimensions) {
        averagedEmbedding[j] += embeddings[i][j] * weight
      }
    }
    for (j in 0 until dimensions) {
      averagedEmbedding[j] /= totalWeight.toFloat()
    }
    return averagedEmbedding
  }

  fun countTokens(text: String?): Int {
    return tokenizer.tokenize(text).size
  }

  private fun loadModel(modelInputStream: InputStream): ByteArray {
    try {
      modelInputStream.use { inputStream ->
        ByteArrayOutputStream().use { buffer ->
          var nRead: Int
          val data = ByteArray(1024)
          while (inputStream.read(data, 0, data.size).also { nRead = it } != -1) {
            buffer.write(data, 0, nRead)
          }
          buffer.flush()
          return buffer.toByteArray()
        }
      }
    } catch (e: IOException) {
      throw RuntimeException(e)
    }
  }

  companion object {
    private const val CLS = "[CLS]"
    private const val SEP = "[SEP]"
    private const val MAX_SEQUENCE_LENGTH = 510 // 512 - 2 (special tokens [CLS] and [SEP])
    private fun partition(wordPieces: List<String>, partitionSize: Int): List<List<String>> {
      val partitions: MutableList<List<String>> = ArrayList()
      var from = 0
      while (from < wordPieces.size) {
        val to = min(wordPieces.size.toDouble(), (from + partitionSize).toDouble()).toInt()
        val partition = wordPieces.subList(from, to)
        partitions.add(partition)
        from += partitionSize
      }
      return partitions
    }

    private fun clsPool(vectors: Array<FloatArray>): FloatArray {
      return vectors[0]
    }

    private fun meanPool(vectors: Array<FloatArray>): FloatArray {
      val numVectors = vectors.size
      val vectorLength = vectors[0].size
      val averagedVector = FloatArray(vectorLength)
      for (vector in vectors) {
        for (j in 0 until vectorLength) {
          averagedVector[j] += vector[j]
        }
      }
      for (j in 0 until vectorLength) {
        averagedVector[j] /= numVectors.toFloat()
      }
      return averagedVector
    }

    private fun normalize(vector: FloatArray): FloatArray {
      var sumSquare = 0f
      for (v in vector) {
        sumSquare += v * v
      }
      val norm = sqrt(sumSquare.toDouble()).toFloat()
      val normalizedVector = FloatArray(vector.size)
      for (i in vector.indices) {
        normalizedVector[i] = vector[i] / norm
      }
      return normalizedVector
    }
  }
}
