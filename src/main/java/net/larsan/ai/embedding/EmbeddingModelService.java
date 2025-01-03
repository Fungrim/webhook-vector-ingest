package net.larsan.ai.embedding;

import net.larsan.ai.api.EmbeddingModelSpec;

public interface EmbeddingModelService {

    EmbeddingFacade getEmbedder(EmbeddingModelSpec model);

}