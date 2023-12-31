import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import dev.spartan.Dependencies

plugins {
  id("org.jetbrains.kotlin.jvm")
}

group = "dev.spartan.langchainkt"
version = "1.0.0"

repositories {
  mavenCentral()
}

subprojects {
  apply(plugin = "kotlin")
  apply(plugin = "java")

  repositories {
    mavenCentral()
    maven(url = "https://maven.google.com/")
  }

  kotlin {
    jvmToolchain {
      languageVersion.set(JavaLanguageVersion.of(17))
    }
  }

  tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
      jvmTarget = "17"
      suppressWarnings = true
    }
  }

  dependencies {
    implementation(Dependencies.Kotlin.stdlib)
    implementation(Dependencies.Kotlin.reflect)

    testImplementation(Dependencies.Testing.jupiter)
    testImplementation(Dependencies.Testing.mockk)
    testImplementation(Dependencies.Testing.strickt)
    testImplementation(Dependencies.Testing.assertj)
  }

  tasks {
    test {
      useJUnitPlatform()
    }
  }
}

