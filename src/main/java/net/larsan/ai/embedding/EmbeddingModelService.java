package net.larsan.ai.embedding;

import net.larsan.ai.api.EmbeddingModel;

public interface EmbeddingModelService {

    EmbeddingFacade getEmbedder(EmbeddingModel model);

}