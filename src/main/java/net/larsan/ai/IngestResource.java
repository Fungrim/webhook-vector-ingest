package net.larsan.ai;

import java.util.List;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.larsan.ai.api.Encoding;
import net.larsan.ai.api.MimeType;
import net.larsan.ai.api.Model;
import net.larsan.ai.api.UpsertRequest;
import net.larsan.ai.docs.TextDocumentParser;
import net.larsan.ai.embed.Database;
import net.larsan.ai.embed.DatabaseService;
import net.larsan.ai.model.ModelService;

@Path("/api/v1")
public class IngestResource {

    @Inject
    DatabaseService dbs;

    @Inject
    ModelService models;

    @Inject
    ChunkingConfig chunkingConfig;

    @GET
    @Path("/embedding/model")
    public List<Model> getEmbeddingModels() {
        return models.getEmbeddingModels();
    }

    @GET
    @Path("/embedding/database")
    public List<String> getDatabases() {
        return dbs.getDatabases();
    }

    @POST
    @Path("/embedding/model/{provider}/{model}/database/{database}")
    public void upsert(
            @PathParam("provider") String provider,
            @PathParam("model") String model,
            @PathParam("database") String db,
            UpsertRequest req) {

        Document d = createDocument(req);

        // transform?

        // chunk
        List<TextSegment> chunks = chunk(d);

        // create embedding
        List<Embedding> embeddings = embed(provider, model, chunks);

        // upsert
        Database database = getDatabase(db);
        embeddings.forEach(e -> {
            database.upsert(req.toUpsert(e));
        });
    }

    private Database getDatabase(String db) {
        return dbs.getDatabase(db);
    }

    private List<Embedding> embed(String provider, String model, List<TextSegment> chunks) {
        return models.getEmbedder(new Model(provider, model)).embedAll(chunks).content();
    }

    private List<TextSegment> chunk(Document d) {
        return new DocumentByParagraphSplitter(chunkingConfig.maxSize(), chunkingConfig.maxOverlap()).split(d);
    }

    private Document createDocument(UpsertRequest req) {
        MimeType t = MimeType.parse(req.mimeType());
        Encoding e = Encoding.parse(req.encoding().orElse("plaintext"));
        if (t == MimeType.TEXT) {
            return new TextDocumentParser().parse(req.content(), e);
        } else {
            throw new UnsupportedOperationException("Not implemented");
        }
    }
}
