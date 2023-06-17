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
