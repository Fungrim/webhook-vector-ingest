package io.github.fungrim.util;

import java.net.URI;

import io.quarkiverse.resteasy.problem.HttpProblem;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Factory for creating HTTP problems that uses a "specificationCode" to enhance the 
 * HTTP status codes. The specification code is appended to the status code to 
 * create a unique identifier for the problem.
 * 
 * For example, a 404 Not Found with specification code 1 would result in a 
 * problem type of "urn:problem:io.github.fungrim:webhook-vector-ingest:404.1".
 */
public class ProblemFactory {

    private ProblemFactory() {
    }

    public static HttpProblem notFound() {   
        return notFound(null, 0, null);
    }

    /**
     * @param entityType Use to construct message "<entityType> not found", optional
     */
    public static HttpProblem notFound(String entityType) {   
        return notFound(entityType, 0, null);
    }

    /**
     * @param entityType Use to construct message "<entityType> not found", optional
     * @param specificationCode Specification code, optional
     */
    public static HttpProblem notFound(String entityType, int specificationCode) {   
        return notFound(entityType, specificationCode, null);
    }

    /**
     * @param entityType Use to construct message "<entityType> not found", optional
     * @param detail Detail message, optional
     */
    public static HttpProblem notFound(String entityType, String detail) {   
        return notFound(entityType, 0, detail);
    }

    /**
     * @param entityType Use to construct message "<entityType> not found", optional
     * @param specificationCode Specification code, optional
     * @param detail Detail message, optional
     */
    public static HttpProblem notFound(String entityType, int specificationCode, String detail) {
        String title = "Entity not found";
        if(!isNullOrEmpty(entityType)) {
            title = entityType + " not found";
        }
        return problem(Response.Status.NOT_FOUND, title, specificationCode, detail);
    }

    private static boolean isNullOrEmpty(String type) {
        return type == null || type.trim().isEmpty();
    }

    /**
     * @param status The HTTP status to use, defaults to INTERNAL_SERVER_ERROR if null
     * @param title The title to use, defaults to "Unknown problem" if null or empty
     * @param specificationCode The specification code to use
     * @param detail The detail message to use, optional
     */
    public static HttpProblem problem(Response.Status status, String title, int specificationCode, String detail) {
        if(status == null) {
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }
        if(isNullOrEmpty(title)) {
            title = "Unknown problem";
        }

        // create specification
        String specification = status.getStatusCode() + (specificationCode < 1 ? "" : "." + specificationCode);
        
        // create builder
        HttpProblem.Builder builder = HttpProblem.builder()
                .withStatus(status)
                .withType(contructType(status, specification))
                .withTitle(title)
                .with("specification", specification);

        // set optional detail
        if(!isNullOrEmpty(detail)) {
            builder = builder.withDetail(detail)
                .withHeader("X-RFC7807-Message", detail);
        }

        // build and return
        return builder.build();
    }

    /**
     * @param specificationCode The specification code to use
     * @param detail The detail message to use, optional
     */
    public static HttpProblem internalError(int specificationCode, String detail) {
        return problem(Response.Status.INTERNAL_SERVER_ERROR, "Internal server error", specificationCode, detail);
    }

    /**
     * @param detail The detail message to use, optional
     */
    public static HttpProblem internalError(String detail) {
        return problem(Response.Status.INTERNAL_SERVER_ERROR, "Internal server error", 0, detail);
    }

    private static URI contructType(Status status, String specification) {
        return URI.create("urn:problem:io.github.fungrim:webhook-vector-ingest:" + specification);
    }
}
