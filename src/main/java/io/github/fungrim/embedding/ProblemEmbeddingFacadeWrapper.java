package io.github.fungrim.embedding;

import java.util.List;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import io.github.fungrim.util.ProblemFactory;
import jakarta.ws.rs.core.Response;

public class ProblemEmbeddingFacadeWrapper implements EmbeddingFacade {

    private final EmbeddingFacade delegate;
    private final String provider;

    public ProblemEmbeddingFacadeWrapper(String provider, EmbeddingFacade delegate) {
        this.provider = provider;
        this.delegate = delegate;
    }

    @Override
    public List<Embedding> embed(String model, List<TextSegment> chunks) {
        try {
            return delegate.embed(model, chunks);
        } catch (Exception e) {
            throw ProblemFactory.problem(Response.Status.INTERNAL_SERVER_ERROR, provider + " exception", 0, e.getMessage());
        }
    }
}
