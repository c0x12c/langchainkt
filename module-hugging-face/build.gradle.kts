import dev.spartan.Dependencies

dependencies {
  api(project(":module-api"))

  implementation(Dependencies.Json.Jackson.databind)
  implementation(Dependencies.Json.Jackson.kotlin)

  implementation(Dependencies.Json.gson)

  implementation(Dependencies.Logging.slf4j)

  implementation(Dependencies.Templating.mustache)

  implementation(Dependencies.LLM.openai4j)
  implementation(Dependencies.LLM.jtokkit)


}
