package dev.spartan

object Dependencies {

  object Kotlin {
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}"
  }

  object Database {
    const val postgresql = "org.postgresql:postgresql:${Versions.postgresql}"
    const val postgisJdbc = "net.postgis:postgis-jdbc:${Versions.postgis}"
    const val hikari = "com.zaxxer:HikariCP:${Versions.hikari}"
  }

  object Logging {
    const val slf4j = "org.slf4j:slf4j-api:2.0.6"
  }

  object Exposed {
    const val core = "org.jetbrains.exposed:exposed-core:${Versions.exposed}"
    const val jdbc = "org.jetbrains.exposed:exposed-jdbc:${Versions.exposed}"
    const val dao = "org.jetbrains.exposed:exposed-dao:${Versions.exposed}"
  }

  object OpenNlp {
    const val tools = "org.apache.opennlp:opennlp-tools:${Versions.opennlp}"
    const val dl = "org.apache.opennlp:opennlp-dl:${Versions.opennlp}"
    const val uima = "org.apache.opennlp:opennlp-uima:${Versions.opennlp}"
  }

  object Json {
    const val moshi = "com.squareup.moshi:moshi:1.14.0"
    const val gson = "com.google.code.gson:gson:2.10.1"

    object Jackson {
      const val api = "com.fasterxml.jackson.core:jackson-annotations:${Versions.jackson}"
      const val core = "com.fasterxml.jackson.core:jackson-core:${Versions.jackson}"
      const val databind = "com.fasterxml.jackson.core:jackson-databind:${Versions.jackson}"
      const val kotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}"
      const val dataTypeJsr310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jackson}"
      const val dataType = "com.fasterxml.jackson.datatype:jackson-datatype-jdk8:${Versions.jackson}"
      const val avro = "com.fasterxml.jackson.dataformat:jackson-dataformat-avro:${Versions.jackson}"
    }
  }

  object Retrofit {
    const val core = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val gson = "com.squareup.retrofit2:converter-gson:${Versions.retrofit}"
    const val jackson = "com.squareup.retrofit2:converter-jackson:${Versions.retrofit}"
    const val rxjava2 = "com.squareup.retrofit2:adapter-rxjava2:${Versions.retrofit}"
    const val scalars = "com.squareup.retrofit2:converter-scalars:${Versions.retrofit}"
  }

  object OkHttp {
    const val core = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val logging = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val urlConnection = "com.squareup.okhttp3:okhttp-urlconnection:${Versions.okhttp}"
    const val mockServer = "com.squareup.okhttp3:mockwebserver:${Versions.okhttp}"
  }

  object Apache {
    const val poi = "org.apache.poi:poi:5.2.3"
  }

  object Utility {
    const val jsoup = "org.jsoup:jsoup:1.16.1"
    const val pdfbox = "org.apache.pdfbox:pdfbox:2.0.29"
  }

  object Templating {
    const val mustache = "com.github.spullara.mustache.java:compiler:0.9.10"
  }

  object Llm {
    const val openai4j = "dev.ai4j:openai4j:0.9.0"
    const val jtokkit = "com.knuddels:jtokkit:0.6.1"
    const val pinecone = "io.pinecone:pinecone-client:0.2.3"
    const val langchain4jEmbedding = "dev.langchain4j:langchain4j-embeddings-all-minilm-l6-v2-q:0.23.0"
  }

  object Testing {
    const val jupiter = "org.junit.jupiter:junit-jupiter:${Versions.junit5}"
    const val mockk = "io.mockk:mockk:1.12.4"
    const val strickt = "io.strikt:strikt-jvm:0.34.0"
    const val assertj = "org.assertj:assertj-core:3.23.1"
  }
}
