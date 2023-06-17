package langchainkt.data.document.source

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import langchainkt.data.document.Document
import langchainkt.data.document.DocumentSource
import langchainkt.data.document.Metadata

class FileSystemSource(
  private val path: Path) : DocumentSource {

  @Throws(IOException::class)
  override fun inputStream(): InputStream? {
    return Files.newInputStream(path)
  }

  override fun metadata(): Metadata {
    return Metadata()
      .add(Document.FILE_NAME, path.fileName)
      .add(Document.ABSOLUTE_DIRECTORY_PATH, path.parent.toAbsolutePath())
  }

  companion object {
    @JvmStatic
    fun from(filePath: Path): FileSystemSource {
      return FileSystemSource(filePath)
    }

    @JvmStatic
    fun from(filePath: String): FileSystemSource {
      return FileSystemSource(Paths.get(filePath))
    }

    @JvmStatic
    fun from(fileUri: URI): FileSystemSource {
      return FileSystemSource(Paths.get(fileUri))
    }

    @JvmStatic
    fun from(file: File): FileSystemSource {
      return FileSystemSource(file.toPath())
    }
  }
}
