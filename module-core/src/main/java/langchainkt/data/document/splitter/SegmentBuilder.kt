package langchainkt.data.document.splitter

import java.util.function.Function
import langchainkt.internal.Validators

internal class SegmentBuilder(
  maxSegmentSize: Int,
  sizeFunction: Function<String, Int>,
  joinSeparator: String
) {
  private val maxSegmentSize: Int
  private val sizeFunction: Function<String, Int>
  private val joinSeparator: String
  private var segmentBuilder: StringBuilder

  init {
    segmentBuilder = StringBuilder()
    this.maxSegmentSize = Validators.ensureGreaterThanZero(maxSegmentSize, "maxSegmentSize")
    this.sizeFunction = Validators.ensureNotNull(sizeFunction, "sizeFunction")
    this.joinSeparator = Validators.ensureNotNull(joinSeparator, "joinSeparator")
  }

  fun hasSpaceFor(text: String): Boolean {
    return if (isNotEmpty) {
      sizeOf(segmentBuilder.toString()) + sizeOf(joinSeparator) + sizeOf(text) <= maxSegmentSize
    } else {
      sizeOf(text) <= maxSegmentSize
    }
  }

  private fun sizeOf(text: String): Int {
    return sizeFunction.apply(text)
  }

  fun append(text: String?) {
    if (isNotEmpty) {
      segmentBuilder.append(joinSeparator)
    }
    segmentBuilder.append(text)
  }

  fun prepend(text: String) {
    if (isNotEmpty) {
      segmentBuilder.insert(0, text + joinSeparator)
    } else {
      segmentBuilder.insert(0, text)
    }
  }

  val isNotEmpty: Boolean
    get() = segmentBuilder.isNotEmpty()

  fun build(): String {
    return segmentBuilder.toString().trim { it <= ' ' }
  }

  fun reset() {
    segmentBuilder = StringBuilder()
  }
}
