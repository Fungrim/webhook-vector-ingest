package net.larsan.ai;

import java.util.List;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.larsan.ai.api.EmbeddingModel;
import net.larsan.ai.api.Encoding;
import net.larsan.ai.api.MimeType;
import net.larsan.ai.api.UpsertRequest;
import net.larsan.ai.api.VectorStorage;
import net.larsan.ai.conf.ChunkingConfig;
import net.larsan.ai.conf.EmbeddingConfig;
import net.larsan.ai.conf.EmbeddingConfig.Model;
import net.larsan.ai.conf.VectorStorageConfig;
import net.larsan.ai.conf.VectorStorageConfig.Storage;
import net.larsan.ai.docs.TextDocumentParser;
import net.larsan.ai.embedding.ModelService;
import net.larsan.ai.storage.Storagager;
import net.larsan.ai.storage.VectorStorageService;

@Path("/api/v1")
public class IngestResource {

    @Inject
    VectorStorageService dbs;

    @Inject
    ModelService models;

    @Inject
    ChunkingConfig chunkingConfig;

    @Inject
    VectorStorageConfig storageConfig;

    @Inject
    EmbeddingConfig embeddingConfig;

    @POST
    @Path("/upsert")
    public void upsert(UpsertRequest req) {
        VectorStorage storage = findStorage(req);
        EmbeddingModel embedding = findEmbedding(req);

        Document d = createDocument(req);

        // transform?

        // chunk
        List<TextSegment> chunks = chunk(d);

        // create embedding
        List<Embedding> embeddings = embed(embedding, chunks);

        // upsert
        Storagager database = getDatabase(storage.provider());
        embeddings.forEach(e -> {
            database.upsert(req.toUpsert(e, storage.namespace()));
        });
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

    private Storagager getDatabase(String db) {
        return dbs.getDatabase(db);
    }

    private List<Embedding> embed(EmbeddingModel model, List<TextSegment> chunks) {
        return models.getEmbedder(model).embed(model.name(), chunks);
    }

    private List<TextSegment> chunk(Document d) {
        return new DocumentByParagraphSplitter(chunkingConfig.maxSize(), chunkingConfig.maxOverlap()).split(d);
    }

    private Document createDocument(UpsertRequest req) {
        MimeType t = MimeType.parse(req.data().mimeType());
        Encoding e = Encoding.parse(req.data().encoding().orElse("plaintext"));
        if (t == MimeType.TEXT) {
            return new TextDocumentParser().parse(req.data().content(), e);
        } else {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
