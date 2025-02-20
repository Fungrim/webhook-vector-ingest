package io.github.fungrim;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.security.SecuritySchemes;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import io.github.fungrim.api.ChunkingSpec;
import io.github.fungrim.api.ChunkingStrategy;
import io.github.fungrim.api.EmbeddingModelSpec;
import io.github.fungrim.api.MetadataField;
import io.github.fungrim.api.UpsertRequest;
import io.github.fungrim.api.VectorStorageSpec;
import io.github.fungrim.conf.ChunkingConfig;
import io.github.fungrim.conf.EmbeddingConfig;
import io.github.fungrim.conf.EmbeddingConfig.Model;
import io.github.fungrim.conf.MetadataConfig;
import io.github.fungrim.conf.VectorStorageConfig;
import io.github.fungrim.conf.VectorStorageConfig.Storage;
import io.github.fungrim.embedding.EmbeddingModelService;
import io.github.fungrim.parser.DocumentParser;
import io.github.fungrim.storage.StorageFacade;
import io.github.fungrim.storage.VectorStorageService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api/v1")
@SecuritySchemes(value = {
        @SecurityScheme(securitySchemeName = "apiKey", type = SecuritySchemeType.APIKEY, apiKeyName = "x-api-key", in = SecuritySchemeIn.HEADER)
})
public class IngestResource {

    @Inject
    VectorStorageService dbs;

    @Inject
    EmbeddingModelService models;

    @Inject
    ChunkingConfig chunkingConfig;

    @Inject
    VectorStorageConfig storageConfig;

    @Inject
    EmbeddingConfig embeddingConfig;

    @Inject
    MetadataConfig metadataConfig;

    @Inject
    Logger log;

    @Inject
    DocumentParser parser;

    @POST
    @Path("/upsert")
    @SecurityRequirement(name = "apiKey")
    public Response upsert(UpsertRequest req) {
        VectorStorageSpec storage = findStorage(req);
        EmbeddingModelSpec embedding = findEmbedding(req);
        Metadata parseMetadata = new Metadata();
        String dataId = req.data().id().orElse("n/a");

        log.infof("Upsert started for document ID %s for storage %s and embedding %s/%s", dataId, storage.provider(), embedding.provider(), embedding.name());

        // parse
        Document d = parser.parse(req, parseMetadata);

        log.debugf("Parsed document ID %s as: %s", dataId, parseMetadata.get(Metadata.CONTENT_TYPE));

        // chunk
        List<TextSegment> chunks = chunk(d, req.chunking());

        log.debugf("Chunked document ID %s in %s chunks", dataId, chunks.size());

        // create embedding
        List<Embedding> embeddings = embed(embedding, chunks);

        // upsert
        StorageFacade database = getStorageFacade(storage.provider());
        embeddings.forEach(e -> {
            database.upsert(req.toUpsert(e, storage.namespace(), transformMetadata(parseMetadata)));
        });

        log.debugf("Document ID %s stored as %s embeddings", dataId, embeddings.size());

        return Response.status(201).build();
    }

    private Optional<List<MetadataField>> transformMetadata(Metadata parseMetadata) {
        if (metadataConfig.fileName().enabled() || metadataConfig.contentType().enabled()) {
            List<MetadataField> fields = new ArrayList<>(2);
            if (metadataConfig.fileName().enabled() && parseMetadata.get(TikaCoreProperties.RESOURCE_NAME_KEY) != null) {
                fields.add(new MetadataField(metadataConfig.fileName().name(), toJson(parseMetadata.get(TikaCoreProperties.RESOURCE_NAME_KEY))));
            }
            if (metadataConfig.contentType().enabled() && parseMetadata.get(Metadata.CONTENT_TYPE) != null) {
                fields.add(new MetadataField(metadataConfig.contentType().name(), toJson(parseMetadata.get(Metadata.CONTENT_TYPE))));
            }
            return Optional.of(fields);
        } else {
            return Optional.empty();
        }
    }

    private ValueNode toJson(String s) {
        return TextNode.valueOf(s);
    }

    private EmbeddingModelSpec findEmbedding(UpsertRequest req) {
        if (req.embedding().isPresent()) {
            return req.embedding().get();
        } else if (embeddingConfig.defaultModel().isPresent()) {
            Model m = embeddingConfig.defaultModel().get();
            return new EmbeddingModelSpec(m.provider(), m.name());
        } else {
            throw new IllegalStateException("No default embedding configured");
        }
    }

    private VectorStorageSpec findStorage(UpsertRequest req) {
        if (req.storage().isPresent()) {
            return req.storage().get();
        } else if (storageConfig.defaultStorage().isPresent()) {
            Storage storage = storageConfig.defaultStorage().get();
            return new VectorStorageSpec(storage.provider(), storage.namespace());
        } else {
            throw new IllegalStateException("No default storage configured");
        }
    }

    private StorageFacade getStorageFacade(String db) {
        return dbs.getDatabase(db);
    }

    private List<Embedding> embed(EmbeddingModelSpec model, List<TextSegment> chunks) {
        return models.getEmbedder(model).embed(model.name(), chunks);
    }

    private List<TextSegment> chunk(Document d, Optional<ChunkingSpec> chunkingSpec) {
        ChunkingStrategy strat = chunkingConfig.strategy();
        int max = chunkingConfig.limits().maxSize();
        int maxOverlap = chunkingConfig.limits().maxOverlap();
        if (chunkingSpec.isPresent()) {
            ChunkingSpec spec = chunkingSpec.get();
            strat = spec.strategy();
            max = spec.limits().maxSize();
            maxOverlap = spec.limits().maxOverlap();
        }
        return strat.create(max, maxOverlap).split(d);
    }
}
