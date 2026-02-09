package io.github.fungrim.util;

import java.net.URI;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openapitools.inference.client.ApiException;

import io.quarkiverse.resteasy.problem.HttpProblem;

public class OpenApiExceptionTest {

    private static final String RESPONSE_BODY = """
            {"error":{"code":"RESOURCE_EXHAUSTED","message":"Request failed. You've reached the max tokens per minute (1000000) for model 'multilingual-e5-large' and input_type 'passage' for the current project. To increase this limit, contact Pinecone Support (https://app.pinecone.io/organizations/-/settings/support/ticket)."},"status":429}""";

    private static final String EXPECTED_MESSAGE = "Request failed. You've reached the max tokens per minute (1000000) for model 'multilingual-e5-large' and input_type 'passage' for the current project. To increase this limit, contact Pinecone Support (https://app.pinecone.io/organizations/-/settings/support/ticket).";

    @Test
    public void shouldParseFromApiException() {
        ApiException apiException = new ApiException("error", 429, null, RESPONSE_BODY);

        OpenApiException parsed = OpenApiException.fromException(apiException);

        Assertions.assertNotNull(parsed.error());
        Assertions.assertEquals("RESOURCE_EXHAUSTED", parsed.error().code());
        Assertions.assertEquals(EXPECTED_MESSAGE, parsed.error().message());
        Assertions.assertEquals(429, parsed.status());
    }

    @Test
    public void shouldConvertToHttpProblem() {
        ApiException apiException = new ApiException("error", 429, null, RESPONSE_BODY);
        OpenApiException parsed = OpenApiException.fromException(apiException);

        HttpProblem problem = parsed.toHttpProblem("Pinecone");

        Assertions.assertEquals(429, problem.getStatusCode());
        Assertions.assertEquals("Pinecone exception", problem.getTitle());
        Assertions.assertEquals(EXPECTED_MESSAGE, problem.getDetail());
        Assertions.assertEquals(URI.create("urn:problem:io.github.fungrim:webhook-vector-ingest:429"), problem.getType());
    }
}
