package net.larsan.ai.embedding;

import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel.OpenAiEmbeddingModelBuilder;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.larsan.ai.OllamaConfig;
import net.larsan.ai.OpenAIConfig;
import net.larsan.ai.api.EmbeddingModel;
import net.larsan.ai.pinecone.Pinecone;
import net.larsan.ai.pinecone.PineconeConfig;

@Singleton
public class ModelService {

    @Inject
    OllamaConfig ollamaConf;

    @Inject
    OpenAIConfig openAiConf;

    @Inject
    PineconeConfig pineconeConfig;

    @Inject
    Instance<Pinecone> pinecone;

    public Embedder getEmbedder(EmbeddingModel model) {
        if ("ollama".equals(model.provider()) && ollamaConf.isLegal()) {
            return (m, l) -> {
                return OllamaEmbeddingModel.builder()
                        .baseUrl(ollamaConf.uri().get())
                        .modelName(model.name())
                        .build().embedAll(l).content();
            };
        }
        if ("openai".equals(model.provider()) && openAiConf.isLegal()) {
            return (m, l) -> {
                OpenAiEmbeddingModelBuilder oai = OpenAiEmbeddingModel.builder()
                        .apiKey(openAiConf.apiKey().get());
                if (openAiConf.url().isPresent()) {
                    oai = oai.baseUrl(openAiConf.url().get());
                }
                return oai.build().embedAll(l).content();
            };
        }
        if ("pinecode".equals(model.provider()) && openAiConf.isLegal()) {
            return (m, l) -> {
                return pinecone.get().embed(m, l);
            };
        }
        throw new IllegalStateException("Embedding model " + model.provider() + "/" + model.name() + " not found");
    }
}
