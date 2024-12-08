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
import net.larsan.ai.docs.TextDocumentParser;
import net.larsan.ai.embedding.ModelService;
import net.larsan.ai.storage.VectorStorage;
import net.larsan.ai.storage.VectorStorageService;

@Path("/api/v1")
public class IngestResource {

    @Inject
    VectorStorageService dbs;

    @Inject
    ModelService models;

    @Inject
    ChunkingConfig chunkingConfig;

    @POST
    @Path("/upsert")
    public void upsert(UpsertRequest req) {

        Document d = createDocument(req);

        // transform?

        // chunk
        List<TextSegment> chunks = chunk(d);

        // create embedding
        List<Embedding> embeddings = embed(req.embedding().provider(), req.embedding().name(), chunks);

        // upsert
        VectorStorage database = getDatabase(req.storage().provider());
        embeddings.forEach(e -> {
            database.upsert(req.toUpsert(e, req.storage().namespace()));
        });
    }

    private VectorStorage getDatabase(String db) {
        return dbs.getDatabase(db);
    }

    private List<Embedding> embed(String provider, String model, List<TextSegment> chunks) {
        return models.getEmbedder(new EmbeddingModel(provider, model)).embed(chunks);
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
