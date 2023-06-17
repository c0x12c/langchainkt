package langchainkt.model.huggingface

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

internal interface HuggingFaceApi {
  @POST("/models/{modelId}")
  @Headers("Content-Type: application/json")
  fun generate(@Body request: TextGenerationRequest?, @Path("modelId") modelId: String?): Call<List<TextGenerationResponse>>

  @POST("/pipeline/feature-extraction/{modelId}")
  @Headers("Content-Type: application/json")
  fun embed(@Body request: EmbeddingRequest?, @Path("modelId") modelId: String?): Call<List<FloatArray>>
}
