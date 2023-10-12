import dev.spartan.Dependencies

dependencies {
  api(project(":module-api"))

  implementation(Dependencies.Logging.slf4j)

  implementation(Dependencies.Apache.poi)

  implementation(Dependencies.OpenNlp.tools)
  implementation(Dependencies.OpenNlp.uima)
  implementation(Dependencies.OpenNlp.dl)

  implementation(Dependencies.Json.Jackson.databind)
  implementation(Dependencies.Json.Jackson.kotlin)

  implementation(Dependencies.Json.gson)

  implementation(Dependencies.Utility.jsoup)
  implementation(Dependencies.Utility.pdfbox)

  implementation(Dependencies.Retrofit.core)
  implementation(Dependencies.OkHttp.core)

  implementation(Dependencies.Templating.mustache)
  testImplementation(project(":embeddings:module-embeddings-all-minilm-l6-v2-q"))
}
