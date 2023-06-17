package langchainkt.model.input.structured

import langchainkt.model.input.structured.StructuredPromptProcessor.toPrompt
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class StructuredPromptProcessorTest {
  
  @Test
  fun test_prompt_with_single_variable() {
    val structuredPrompt = Greeting()
    structuredPrompt.name = "Klaus"
    val text = toPrompt(structuredPrompt).text()
    assertThat(text).isEqualTo("Hello, my name is Klaus")
  }

  @Test
  fun test_prompt_with_multiple_variables() {
    val structuredPrompt = SuggestRecipes()
    structuredPrompt.dish = "salad"
    structuredPrompt.maxPreparationTime = 5
    structuredPrompt.ingredients = mutableListOf("Tomato", "Cucumber", "Onion")
    val text = toPrompt(structuredPrompt).text()
    assertThat(text).isEqualTo("Suggest tasty salad recipes that can be prepared in 5 minutes.\nI have only [Tomato, Cucumber, Onion] in my fridge.")
  }

  @Test
  fun test_prompt_with_various_number_types() {
    val numbers = VariousNumbers()
    numbers.nDouble = 17.15
    numbers.nFloat = 1f
    numbers.nInt = 2
    numbers.nShort = 10
    numbers.nLong = 12
    val text = toPrompt(numbers).text()
    assertThat(text).isEqualTo("Example of numbers with floating point: 17.15, 1.0 and whole numbers: 2, 10, 12")
  }

  @StructuredPrompt("Hello, my name is {{name}}")
  internal class Greeting {
    var name: String? = null
  }

  @StructuredPrompt(*arrayOf("Suggest tasty {{dish}} recipes that can be prepared in {{maxPreparationTime}} minutes.", "I have only {{ingredients}} in my fridge."))
  internal class SuggestRecipes {
    var dish: String? = null
    var maxPreparationTime = 0
    var ingredients: List<String>? = null
  }

  @StructuredPrompt("Example of numbers with floating point: {{nDouble}}, {{nFloat}} and whole numbers: {{nInt}}, {{nShort}}, {{nLong}}")
  internal class VariousNumbers {
    var nDouble = 0.0
    var nFloat = 0f
    var nInt = 0
    var nShort: Short = 0
    var nLong: Long = 0
  }
}
