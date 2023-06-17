package langchainkt.internal

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource

class ValidatorsTest {

  @ParameterizedTest
  @ValueSource(ints = [1, Int.MAX_VALUE])
  fun should_not_throw_when_greater_than_0(i: Int?) {
    Validators.ensureGreaterThanZero(i, "integer")
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(ints = [Int.MIN_VALUE, 0])
  fun should_throw_when_when_not_greater_than_0(i: Int?) {
    assertThatThrownBy { Validators.ensureGreaterThanZero(i, "integer") }
      .isExactlyInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("integer must be greater than zero, but is: $i")
  }

  @ParameterizedTest
  @ValueSource(doubles = [0.0, 0.5, 1.0])
  fun should_not_throw_when_between(d: Double?) {
    Validators.ensureBetween(d, 0.0, 1.0, "test")
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(doubles = [-0.1, 1.1])
  fun should_throw_when_not_between(d: Double?) {
    assertThatThrownBy { Validators.ensureBetween(d, 0.0, 1.0, "test") }
      .isExactlyInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("test must be between 0.0 and 1.0, but is: $d")
  }
}
