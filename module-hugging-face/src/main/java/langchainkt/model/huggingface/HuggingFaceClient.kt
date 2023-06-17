package langchainkt.model.huggingface

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import java.io.IOException
import java.time.Duration
import langchainkt.internal.Validators.ensureNotBlank
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal class HuggingFaceClient(
  apiKey: String,
  modelId: String?,
  timeout: Duration
) {
  private val huggingFaceApi: HuggingFaceApi
  private val modelId: String

  init {
    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
      .addInterceptor(ApiKeyInsertingInterceptor(apiKey))
      .callTimeout(timeout)
      .connectTimeout(timeout)
      .readTimeout(timeout)
      .writeTimeout(timeout)
      .build()
    val gson = GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .create()
    val retrofit = Retrofit.Builder()
      .baseUrl("https://api-inference.huggingface.co")
      .client(okHttpClient)
      .addConverterFactory(GsonConverterFactory.create(gson))
      .build()
    huggingFaceApi = retrofit.create(HuggingFaceApi::class.java)
    this.modelId = ensureNotBlank(modelId, "modelId")
  }

  fun chat(request: TextGenerationRequest): TextGenerationResponse {
    return generate(request)
  }

  fun generate(request: TextGenerationRequest): TextGenerationResponse {
    return try {
      val retrofitResponse = huggingFaceApi.generate(request, modelId).execute()
      if (retrofitResponse.isSuccessful) {
        toOneResponse(retrofitResponse)
      } else {
        throw toException(retrofitResponse)
      }
    } catch (e: IOException) {
      throw RuntimeException(e)
    }
  }

  fun embed(request: EmbeddingRequest): List<FloatArray> {
    return try {
      val retrofitResponse = huggingFaceApi.embed(request, modelId).execute()
      if (retrofitResponse.isSuccessful) {
        retrofitResponse.body()!!
      } else {
        throw toException(retrofitResponse)
      }
    } catch (e: IOException) {
      throw RuntimeException(e)
    }
  }

  companion object {
    private fun toOneResponse(retrofitResponse: Response<List<TextGenerationResponse>>): TextGenerationResponse {
      val responses = retrofitResponse.body()
      return if (responses != null && responses.size == 1) {
        responses[0]
      } else {
        throw RuntimeException("Expected only one generated_text, but was: " + (responses?.size ?: 0))
      }
    }

    @Throws(IOException::class)
    private fun toException(response: Response<*>): RuntimeException {
      val code = response.code()
      val body = response.errorBody()!!.string()
      val errorMessage = String.format("status code: %s; body: %s", code, body)
      return RuntimeException(errorMessage)
    }
  }
}
