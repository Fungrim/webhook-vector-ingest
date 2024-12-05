package net.larsan.ai.model;

import java.util.List;

import dev.langchain4j.model.embedding.EmbeddingModel;
import jakarta.inject.Singleton;
import net.larsan.ai.api.Model;

@Singleton
public class ModelService {

    public List<Model> getEmbeddingModels() {
        return null;
    }

    public EmbeddingModel getEmbedder(Model model) {
        return null;
    }
}
