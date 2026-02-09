package io.github.fungrim.pinecone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openapitools.inference.client.ApiException;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import io.github.fungrim.conf.PineconeConfig;
import io.github.fungrim.util.OpenApiException;
import io.github.fungrim.util.ProblemFactory;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.pinecone.clients.Inference;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;

public class PineconeEmbedder {

    private final Inference infererence;
    private final PineconeConfig conf;

    public PineconeEmbedder(Inference infererence, PineconeConfig conf) {
        this.infererence = infererence;
        this.conf = conf;
    }

    public List<Embedding> embed(String model, List<TextSegment> chunks) {
        List<String> texts = chunks.stream().map(c -> c.text()).toList();
        List<Embedding> allEmbeddings = new ArrayList<>();
        int batchSize = conf.embeddingBatchSize();
        for (int i = 0; i < texts.size(); i += batchSize) {
            int end = Math.min(i + batchSize, texts.size());
            List<String> batch = texts.subList(i, end);
            try {
                if(isRetryEnabled()) {
                    List<Embedding> batchEmbeddings = callPineconeWithRetry(model, batch);
                    allEmbeddings.addAll(batchEmbeddings);
                } else {
                    List<Embedding> batchEmbeddings = callPinecone(model, batch);
                    allEmbeddings.addAll(batchEmbeddings);
                }
            } catch (ApiException e) {
                throw OpenApiException.fromException(e).toHttpProblem("Pinecone");
            }
        }
        
        return allEmbeddings;
    }

    private List<Embedding> callPineconeWithRetry(String model, List<String> batch) {
        IntervalFunction intervalFunction = createIntervalFunction();
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(conf.rateLimitRetry().maxAttempts())
            .intervalFunction(intervalFunction)
            .failAfterMaxAttempts(true)
            .retryOnException(e -> e instanceof ApiException ex && ex.getCode() == 429)
            .build();
        Retry retry = Retry.of("pinecone", config);
        try {
            return retry.executeCheckedSupplier(() -> callPinecone(model, batch));
        } catch(ApiException e) {
            if(e.getCode() == 429) {
                throw ProblemFactory.problem(Response.Status.TOO_MANY_REQUESTS, "Pinecone retry error", 1, "Max retries exceeded");
            } else {
                throw OpenApiException.fromException(e).toHttpProblem("Pinecone");
            }
        } catch(Throwable t) {
            Log.error("Pinecone retry error", t);
            throw ProblemFactory.problem(Response.Status.INTERNAL_SERVER_ERROR, "Pinecone retry error", 0, t.getMessage());
        }
    }

    private IntervalFunction createIntervalFunction() {
       if(conf.rateLimitRetry().exponantionalBackoff()) {
           return IntervalFunction.ofExponentialBackoff(conf.rateLimitRetry().baseInterval());
       } else {
           return IntervalFunction.of(conf.rateLimitRetry().baseInterval());
       }
    }

    private boolean isRetryEnabled() {
        return conf.rateLimitRetry() != null && conf.rateLimitRetry().enabled();
    }

    private List<Embedding> callPinecone(String model, List<String> batch) throws ApiException {
        return infererence.embed(model, 
                Collections.singletonMap("input_type", "passage"), batch)
                .getData().stream()
                .map(e -> new Embedding(toFloats(e)))
                .toList();
    }

    @SuppressWarnings("null")
    private float[] toFloats(org.openapitools.inference.client.model.Embedding e) {
        if (e.getValues() == null || e.getValues().size() == 0) {
            return new float[0];
        } else {
            float[] a = new float[e.getValues().size()];
            for (int i = 0; i < a.length; i++) {
                a[i] = e.getValues().get(i).floatValue();
            }
            return a;
        }
    }
}
