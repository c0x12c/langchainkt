package langchainkt.data.document.source

import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import langchainkt.data.document.Document
import langchainkt.data.document.DocumentSource
import langchainkt.data.document.Metadata

class UrlSource(
  private val url: URL
) : DocumentSource {

  @Throws(IOException::class)
  override fun inputStream(): InputStream? {
    val connection = url.openConnection()
    return connection.getInputStream()
  }

  override fun metadata(): Metadata {
    return Metadata.from(Document.URL, url)
  }

  companion object {
    fun from(url: String?): UrlSource {
      return try {
        UrlSource(URL(url))
      } catch (e: MalformedURLException) {
        throw RuntimeException(e)
      }
    }

    @JvmStatic
    fun from(url: URL): UrlSource {
      return UrlSource(url)
    }

    fun from(uri: URI): UrlSource {
      return try {
        UrlSource(uri.toURL())
      } catch (e: MalformedURLException) {
        throw RuntimeException(e)
      }
    }
  }
}
