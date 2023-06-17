import dev.spartan.Dependencies

dependencies {
  implementation(Dependencies.Json.Jackson.databind)
  implementation(Dependencies.Json.Jackson.kotlin)

  implementation(Dependencies.Json.gson)

  implementation(Dependencies.Logging.slf4j)

  implementation(Dependencies.Templating.mustache)
}
