package net.larsan.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.larsan.ai.api.EmbeddingModel;
import net.larsan.ai.api.MetadataField;
import net.larsan.ai.api.UpsertRequest;
import net.larsan.ai.api.VectorStorage;
import net.larsan.ai.conf.ChunkingConfig;
import net.larsan.ai.conf.EmbeddingConfig;
import net.larsan.ai.conf.EmbeddingConfig.Model;
import net.larsan.ai.conf.MetadataConfig;
import net.larsan.ai.conf.VectorStorageConfig;
import net.larsan.ai.conf.VectorStorageConfig.Storage;
import net.larsan.ai.embedding.EmbeddingModelService;
import net.larsan.ai.parser.DocumentParser;
import net.larsan.ai.storage.StorageFacade;
import net.larsan.ai.storage.VectorStorageService;

@Path("/api/v1")
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
    public void upsert(UpsertRequest req) {
        VectorStorage storage = findStorage(req);
        EmbeddingModel embedding = findEmbedding(req);
        Metadata parseMetadata = new Metadata();

        // parse
        Document d = parser.parse(req, parseMetadata);

        // chunk
        List<TextSegment> chunks = chunk(d);

        // create embedding
        List<Embedding> embeddings = embed(embedding, chunks);

        // upsert
        StorageFacade database = getStorageFacade(storage.provider());
        embeddings.forEach(e -> {
            database.upsert(req.toUpsert(e, storage.namespace(), transformMetadata(parseMetadata)));
        });
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

    private EmbeddingModel findEmbedding(UpsertRequest req) {
        if (req.embedding().isPresent()) {
            return req.embedding().get();
        } else if (embeddingConfig.defaultModel().isPresent()) {
            Model m = embeddingConfig.defaultModel().get();
            return new EmbeddingModel(m.provider(), m.name());
        } else {
            throw new IllegalStateException("No default embedding configured");
        }
    }

    private VectorStorage findStorage(UpsertRequest req) {
        if (req.storage().isPresent()) {
            return req.storage().get();
        } else if (storageConfig.defaultStorage().isPresent()) {
            Storage storage = storageConfig.defaultStorage().get();
            return new VectorStorage(storage.provider(), storage.namespace());
        } else {
            throw new IllegalStateException("No default storage configured");
        }
    }

    private StorageFacade getStorageFacade(String db) {
        return dbs.getDatabase(db);
    }

    private List<Embedding> embed(EmbeddingModel model, List<TextSegment> chunks) {
        return models.getEmbedder(model).embed(model.name(), chunks);
    }

    private List<TextSegment> chunk(Document d) {
        return new DocumentByParagraphSplitter(chunkingConfig.maxSize(), chunkingConfig.maxOverlap()).split(d);
    }
}
