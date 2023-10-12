pluginManagement {
  repositories {
    mavenCentral()
    maven(url = "https://plugins.gradle.org/m2/")
    maven(url = "https://maven.google.com/")
    gradlePluginPortal()
  }

  plugins {
    id("org.jetbrains.kotlin.jvm") version ("1.9.10")
  }
}

rootProject.name = "langchainkt"

include(
  "module-api",
  "module-core",
  "module-open-ai",
  "module-hugging-face",
  "module-pinecone"
)

include(
  "embeddings:module-embeddings",
  "embeddings:module-embeddings-all-minilm-l6-v2",
  "embeddings:module-embeddings-all-minilm-l6-v2-q",
  "embeddings:module-embeddings-bge-small-en",
  "embeddings:module-embeddings-bge-small-en-q",
  "embeddings:module-embeddings-bge-small-zh",
  "embeddings:module-embeddings-bge-small-zh-q",
  "embeddings:module-embeddings-e5-small-v2",
  "embeddings:module-embeddings-e5-small-v2-q"
)
