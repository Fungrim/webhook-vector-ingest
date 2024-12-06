package net.larsan.ai.model;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import dev.langchain4j.model.embedding.EmbeddingModel;
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import net.larsan.ai.api.Model;

@Singleton
public class ModelService {

    @Inject
    OllamaConfig ollamaConf;

    public List<Model> getEmbeddingModels() {
        List<Model> l = new ArrayList<>();
        if (ollamaConf.isLegal()) {
            OllamaAPI ollamaAPI = new OllamaAPI(ollamaConf.uri().get());
            try {
                ollamaAPI.listModels().forEach(m -> l.add(new Model("ollama", m.getName())));
            } catch (OllamaBaseException | IOException | InterruptedException | URISyntaxException e) {
                throw new IllegalStateException(e);
            }
        }
        return l;
    }

    public EmbeddingModel getEmbedder(Model model) {
        return null;
    }
}
