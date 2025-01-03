package net.larsan.ai;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import jakarta.enterprise.context.ApplicationScoped;
import net.larsan.ai.api.ChunkingSpec;
import net.larsan.ai.api.ChunkingStrategy;
import net.larsan.ai.api.EmbeddingModelSpec;
import net.larsan.ai.api.UpsertRequest;
import net.larsan.ai.api.UpsertRequest.Data;
import net.larsan.ai.api.VectorStorageSpec;
import net.larsan.ai.embedding.EmbeddingFacade;
import net.larsan.ai.embedding.EmbeddingModelService;
import net.larsan.ai.storage.StorageFacade;
import net.larsan.ai.storage.VectorStorageService;

@QuarkusTest
public class IngestResourceTest {

    @Test
    public void testOnlyData() {
        Data d = new Data(Optional.of("666"), Optional.empty(), Optional.empty(), "{ \"text\" = \"Hello World!\" }", Optional.empty(), Optional.empty());
        UpsertRequest req = new UpsertRequest(Optional.empty(), Optional.empty(), Optional.empty(), d);
        RestAssured.given()
                .contentType("application/json")
                .body(req)
                .when()
                .post("/api/v1/upsert")
                .then()
                .statusCode(201);
    }

    @Test
    public void testFull() {
        Data d = new Data(Optional.of("667"), Optional.of("application/json"), Optional.of("hello.json"), "{ \"text\" = \"Hello World!\" }", Optional.empty(), Optional.empty());
        UpsertRequest req = new UpsertRequest(Optional.of(new EmbeddingModelSpec("ollama", "model2")), Optional.of(new VectorStorageSpec("pinecone", Optional.empty())),
                Optional.of(new ChunkingSpec(ChunkingStrategy.PARAGRAPH, new ChunkingSpec.Limits(512, 128))), d);
        RestAssured.given()
                .contentType("application/json")
                .body(req)
                .when()
                .post("/api/v1/upsert")
                .then()
                .statusCode(201);
    }

    @Mock
    @ApplicationScoped
    public static class ModelService implements EmbeddingModelService {

        @Override
        public EmbeddingFacade getEmbedder(EmbeddingModelSpec model) {
            return (m, c) -> List.of();
        }
    }

    @Mock
    @ApplicationScoped
    public static class DbService implements VectorStorageService {

        @Override
        public StorageFacade getDatabase(String db) {
            return (req) -> {
            };
        }
    }
}
