import dev.spartan.Dependencies

dependencies {
  api(project(":module-api"))
  implementation(Dependencies.LLM.onnxruntime)
  implementation(Dependencies.LLM.djl)
}
