package langchainkt.model.embedding

import java.nio.file.Path
import java.nio.file.Paths

/**
 * An embedding model that runs within your Java application's process.
 * Any BERT-based model (e.g., from HuggingFace) can be used, as long as it is in ONNX format.
 * Information on how to convert models into ONNX format can be found [here](https://huggingface.co/docs/optimum/exporters/onnx/usage_guides/export_a_model).
 * Many models already converted to ONNX format are available [here](https://huggingface.co/Xenova).
 *
 * @param pathToModel The path to the .onnx model file (e.g., "/home/me/model.onnx").
 */
class OnnxEmbeddingModel(
  pathToModel: Path
) : AbstractInProcessEmbeddingModel() {
  private val model: OnnxBertBiEncoder

  init {
    model = loadFromFileSystem(pathToModel)
  }

  /**
   * @param pathToModel The path to the .onnx model file (e.g., "/home/me/model.onnx").
   */
  constructor(pathToModel: String) : this(Paths.get(pathToModel))

  override fun model(): OnnxBertBiEncoder {
    return model
  }
}
