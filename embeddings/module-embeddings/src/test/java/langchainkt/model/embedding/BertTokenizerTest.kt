package langchainkt.model.embedding

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class BertTokenizerTest {
  private var tokenizer = BertTokenizer()

  @Test
  fun should_count_tokens_in_text() {
    val tokenCount = tokenizer.estimateTokenCountInText("Hello, how are you doing?")
    assertThat(tokenCount).isEqualTo(7)
  }

  @Test
  fun should_tokenize() {
    val tokens = tokenizer.tokenize("Hello, how are you doing?")
    assertThat(tokens).containsExactly(
      "hello",
      ",",
      "how",
      "are",
      "you",
      "doing",
      "?"
    )
  }

  @Test
  fun should_return_token_id() {
    assertThat(tokenizer.tokenId("[CLS]")).isEqualTo(101)
    assertThat(tokenizer.tokenId("[SEP]")).isEqualTo(102)
    assertThat(tokenizer.tokenId("hello")).isEqualTo(7592)
  }
}
