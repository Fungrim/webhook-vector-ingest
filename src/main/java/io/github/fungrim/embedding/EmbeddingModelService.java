package io.github.fungrim.embedding;

import io.github.fungrim.api.EmbeddingModelSpec;

public interface EmbeddingModelService {

    EmbeddingFacade getEmbedder(EmbeddingModelSpec model);

}