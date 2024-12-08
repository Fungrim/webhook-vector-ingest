package net.larsan.ai.pinecone;

import java.util.List;

import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import io.pinecone.clients.Index;
import io.pinecone.clients.Inference;
import net.larsan.ai.api.MetadataField;
import net.larsan.ai.embedding.Embedder;
import net.larsan.ai.storage.VectorStorage;

public class Pinecone implements VectorStorage, Embedder {

    private final io.pinecone.clients.Pinecone client;
    private final Index index;
    private Inference infererence;

    public Pinecone(PineconeConfig conf) {
        client = new io.pinecone.clients.Pinecone.Builder(conf.apiKey().get()).build();
        index = client.getIndexConnection(conf.index().get());
        infererence = client.getInferenceClient();
    }

    public void upsert(Upsert req) {
        index.upsert(req.id(), req.values(), null, null, toStruct(req.metadata()), req.namespace());
    }

    @Override
    public List<Embedding> embed(List<TextSegment> chunks) {
        return null;
    }

    private Struct toStruct(List<MetadataField> metadata) {
        Builder b = Struct.newBuilder();
        metadata.forEach(f -> {
            b.putFields(f.name(), f.toProtobufValue());
        });
        return b.build();
    }
}
