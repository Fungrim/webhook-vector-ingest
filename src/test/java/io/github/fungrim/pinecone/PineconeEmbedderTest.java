package io.github.fungrim.pinecone;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.inference.client.ApiException;
import org.openapitools.inference.client.model.EmbeddingsList;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import io.github.fungrim.conf.PineconeConfig;
import io.pinecone.clients.Inference;
import io.quarkiverse.resteasy.problem.HttpProblem;

public class PineconeEmbedderTest {

    private Inference inference;
    private PineconeConfig config;
    private PineconeConfig.Retry retryConfig;

    @BeforeEach
    public void setUp() {
        inference = mock(Inference.class);
        config = mock(PineconeConfig.class);
        retryConfig = mock(PineconeConfig.Retry.class);
        when(config.rateLimitRetry()).thenReturn(retryConfig);
        when(config.embeddingBatchSize()).thenReturn(96);
    }

    @Test
    public void shouldReturnEmbeddingsWithoutRetry() throws ApiException {
        when(retryConfig.enabled()).thenReturn(false);
        when(inference.embed(anyString(), anyMap(), anyList())).thenReturn(embeddingsList(0.1f, 0.2f));

        PineconeEmbedder embedder = new PineconeEmbedder(inference, config);
        List<Embedding> result = embedder.embed("test-model", List.of(TextSegment.from("hello")));

        Assertions.assertEquals(1, result.size());
        Assertions.assertArrayEquals(new float[]{0.1f, 0.2f}, result.get(0).vector());
        verify(inference, times(1)).embed(anyString(), anyMap(), anyList());
    }

    @Test
    public void shouldRetryOn429AndSucceed() throws ApiException {
        configureRetry(true, 3, Duration.ofMillis(10), false);
        when(inference.embed(anyString(), anyMap(), anyList()))
                .thenThrow(new ApiException("rate limited", 429, null, null))
                .thenReturn(embeddingsList(0.5f));

        PineconeEmbedder embedder = new PineconeEmbedder(inference, config);
        List<Embedding> result = embedder.embed("test-model", List.of(TextSegment.from("hello")));

        Assertions.assertEquals(1, result.size());
        Assertions.assertArrayEquals(new float[]{0.5f}, result.get(0).vector());
        verify(inference, times(2)).embed(anyString(), anyMap(), anyList());
    }

    @Test
    public void shouldThrowAfterMaxRetriesExhausted() throws ApiException {
        configureRetry(true, 2, Duration.ofMillis(10), false);
        when(inference.embed(anyString(), anyMap(), anyList()))
                .thenThrow(new ApiException("rate limited", 429, null, null));

        PineconeEmbedder embedder = new PineconeEmbedder(inference, config);

        HttpProblem thrown = Assertions.assertThrows(HttpProblem.class,
                () -> embedder.embed("test-model", List.of(TextSegment.from("hello"))));

        Assertions.assertEquals(429, thrown.getStatusCode());
        verify(inference, times(2)).embed(anyString(), anyMap(), anyList());
    }

    @Test
    public void shouldNotRetryOnNon429Error() throws ApiException {
        configureRetry(true, 3, Duration.ofMillis(10), false);
        when(inference.embed(anyString(), anyMap(), anyList()))
                .thenThrow(new ApiException("server error", 500, null, null));

        PineconeEmbedder embedder = new PineconeEmbedder(inference, config);

        HttpProblem thrown = Assertions.assertThrows(HttpProblem.class,
                () -> embedder.embed("test-model", List.of(TextSegment.from("hello"))));

        Assertions.assertEquals(500, thrown.getStatusCode());
        verify(inference, times(1)).embed(anyString(), anyMap(), anyList());
    }

    @Test
    public void shouldNotRetryWhenRetryDisabled() throws ApiException {
        when(retryConfig.enabled()).thenReturn(false);
        String responseBody = """
                {"error":{"code":"RESOURCE_EXHAUSTED","message":"Rate limited"},"status":429}""";
        when(inference.embed(anyString(), anyMap(), anyList()))
                .thenThrow(new ApiException("rate limited", 429, null, responseBody));

        PineconeEmbedder embedder = new PineconeEmbedder(inference, config);

        HttpProblem thrown = Assertions.assertThrows(HttpProblem.class,
                () -> embedder.embed("test-model", List.of(TextSegment.from("hello"))));

        Assertions.assertEquals(429, thrown.getStatusCode());
        verify(inference, times(1)).embed(anyString(), anyMap(), anyList());
    }

    @Test
    public void shouldRetryWithExponentialBackoff() throws ApiException {
        configureRetry(true, 3, Duration.ofMillis(10), true);
        when(inference.embed(anyString(), anyMap(), anyList()))
                .thenThrow(new ApiException("rate limited", 429, null, null))
                .thenThrow(new ApiException("rate limited", 429, null, null))
                .thenReturn(embeddingsList(1.0f));

        PineconeEmbedder embedder = new PineconeEmbedder(inference, config);
        List<Embedding> result = embedder.embed("test-model", List.of(TextSegment.from("hello")));

        Assertions.assertEquals(1, result.size());
        verify(inference, times(3)).embed(anyString(), anyMap(), anyList());
    }

    private void configureRetry(boolean enabled, int maxAttempts, Duration baseInterval, boolean exponentialBackoff) {
        when(retryConfig.enabled()).thenReturn(enabled);
        when(retryConfig.maxAttempts()).thenReturn(maxAttempts);
        when(retryConfig.baseInterval()).thenReturn(baseInterval);
        when(retryConfig.exponantionalBackoff()).thenReturn(exponentialBackoff);
    }

    private EmbeddingsList embeddingsList(float... values) {
        org.openapitools.inference.client.model.Embedding embedding =
                new org.openapitools.inference.client.model.Embedding();
        List<BigDecimal> decimals = new java.util.ArrayList<>();
        for (float v : values) {
            decimals.add(BigDecimal.valueOf(v));
        }
        embedding.setValues(decimals);
        EmbeddingsList list = new EmbeddingsList();
        list.setData(List.of(embedding));
        return list;
    }
}
