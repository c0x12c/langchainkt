package langchainkt.code

import java.time.Duration
import java.util.Base64
import langchainkt.internal.Json
import langchainkt.internal.Utils
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory

internal class Judge0JavaScriptEngine(
  private val apiKey: String,
  private val languageId: Int, timeout: Duration
) : CodeExecutionEngine {
  private val client: OkHttpClient

  init {
    client = OkHttpClient.Builder()
      .connectTimeout(timeout)
      .readTimeout(timeout)
      .writeTimeout(timeout)
      .callTimeout(timeout)
      .build()
  }

  override fun execute(code: String): String {
    val base64EncodedCode = Base64.getEncoder().encodeToString(code.toByteArray())
    val submission = Submission(languageId, base64EncodedCode)
    val requestBody: RequestBody = Json.toJson(submission).toRequestBody(MEDIA_TYPE)
    val request: Request = Request.Builder()
      .url("https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=true&wait=true&fields=*")
      .addHeader("X-RapidAPI-Key", apiKey)
      .post(requestBody)
      .build()
    return try {
      val response = client.newCall(request).execute()
      val responseBody = response.body!!.string()
      val result = Json.fromJson(responseBody, SubmissionResult::class.java)
      if (result.status!!.id != ACCEPTED) {
        var error = result.status!!.description
        if (!Utils.isNullOrBlank(result.compileOutput)) {
          error += "\n"
          error += String(Base64.getMimeDecoder().decode(result.compileOutput))
        }
        return error!!
      }
      val base64EncodedStdout = result.stdout ?: return "No result: nothing was printed out to the console"
      String(Base64.getMimeDecoder().decode(base64EncodedStdout.trim { it <= ' ' })).trim { it <= ' ' }
    } catch (e: Exception) {
      log.warn("Error during code execution", e)
      e.message!!
    }
  }

  private class Submission(
    var languageId: Int,
    var sourceCode: String
  )

  private class SubmissionResult {
    var stdout: String? = null
    var status: Status? = null
    var compileOutput: String? = null
  }

  private class Status {
    var id = 0
    var description: String? = null
  }

  companion object {
    private val log = LoggerFactory.getLogger(Judge0JavaScriptEngine::class.java)
    private val MEDIA_TYPE: MediaType = "application/json".toMediaType()
    private const val ACCEPTED = 3
  }
}
