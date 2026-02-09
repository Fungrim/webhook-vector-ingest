package io.github.fungrim.util;

import org.openapitools.inference.client.ApiException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;

public record OpenApiException(
    OpenApiExceptionBody error,
    int status
) {

    public HttpProblem toHttpProblem(String provider) {
        return ProblemFactory.problem(
            Response.Status.fromStatusCode(status),
            provider + " exception",
            0,
            error == null ? null : error.message()
        );
    }

    public static OpenApiException fromException(ApiException e) {
        try {
            return new ObjectMapper().readValue(e.getResponseBody(), OpenApiException.class);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to parse OpenAPI exception", ex);
        }
    }
}
