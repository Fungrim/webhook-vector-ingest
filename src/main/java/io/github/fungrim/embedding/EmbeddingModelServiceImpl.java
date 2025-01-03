package io.github.fungrim.embedding;

import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel.OpenAiEmbeddingModelBuilder;
import io.github.fungrim.api.EmbeddingModelSpec;
import io.github.fungrim.conf.OllamaConfig;
import io.github.fungrim.conf.OpenAIConfig;
import io.github.fungrim.conf.PineconeConfig;
import io.github.fungrim.pinecone.Pinecone;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class EmbeddingModelServiceImpl implements EmbeddingModelService {

    @Inject
    OllamaConfig ollamaConf;

    @Inject
    OpenAIConfig openAiConf;

    @Inject
    PineconeConfig pineconeConfig;

    @Inject
    Instance<Pinecone> pinecone;

    @Override
    public EmbeddingFacade getEmbedder(EmbeddingModelSpec model) {
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
                if (openAiConf.uri().isPresent()) {
                    oai = oai.baseUrl(openAiConf.uri().get());
                }
                return oai.build().embedAll(l).content();
            };
        }
        if ("pinecone".equals(model.provider()) && pineconeConfig.isLegal()) {
            return (m, l) -> {
                return pinecone.get().embed(m, l);
            };
        }
        throw new IllegalStateException("Embedding model " + model.provider() + "/" + model.name() + " not found, or configuration is invalid");
    }
}
