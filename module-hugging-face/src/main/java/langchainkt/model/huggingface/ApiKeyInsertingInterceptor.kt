package langchainkt.model.huggingface

import java.io.IOException
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class ApiKeyInsertingInterceptor(
  private val apiKey: String
) : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val request: Request = chain.request()
      .newBuilder()
      .addHeader("Authorization", "Bearer $apiKey")
      .build()
    return chain.proceed(request)
  }
}
