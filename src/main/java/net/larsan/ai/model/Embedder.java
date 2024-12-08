package net.larsan.ai.model;

import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;

@FunctionalInterface
public interface Embedder {

    public List<Embedding> embed(List<TextSegment> chunks);

}
