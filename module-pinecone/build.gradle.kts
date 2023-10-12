import dev.spartan.Dependencies

dependencies {
  api(project(":module-api"))
  implementation(Dependencies.LLM.pinecone)
  testImplementation(project(":embeddings:module-embeddings"))
}
