package net.larsan.ai.embedding;

import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;

@FunctionalInterface
public interface EmbeddingFacade {

    public List<Embedding> embed(String model, List<TextSegment> chunks);

}
