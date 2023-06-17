import dev.spartan.Dependencies

dependencies {
  api(project(":module-api"))
  implementation(Dependencies.Llm.pinecone)
  implementation(Dependencies.Llm.langchain4jEmbedding)
}
