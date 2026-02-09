package io.github.fungrim.pinecone;

import java.util.List;
import java.util.stream.Stream;

import com.google.protobuf.Struct;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import io.github.fungrim.api.MetadataField;
import io.github.fungrim.conf.PineconeConfig;
import io.github.fungrim.embedding.EmbeddingFacade;
import io.github.fungrim.storage.StorageFacade;
import io.pinecone.clients.Index;

public class Pinecone implements StorageFacade, EmbeddingFacade {

    private final PineconeEmbedder embedder;
    private final io.pinecone.clients.Pinecone client;
    private final Index index;

    public Pinecone(PineconeConfig conf) {
        client = new io.pinecone.clients.Pinecone.Builder(conf.apiKey().get()).build();
        index = client.getIndexConnection(conf.index().get());
        embedder = new PineconeEmbedder(client.getInferenceClient(), conf);
    }

    public void upsert(Upsert req) {
        index.upsert(req.id(), req.values(), null, null, toStruct(req.metadata()), req.namespace().orElse(null));
    }

    @Override
    public List<Embedding> embed(String model, List<TextSegment> chunks) {
        return embedder.embed(model, chunks);
    }

    private Struct toStruct(Stream<MetadataField> metadata) {
        com.google.protobuf.Struct.Builder b = Struct.newBuilder();
        metadata.forEach(f -> {
            b.putFields(f.key(), f.toProtobufValue());
        });
        return b.build();
    }
}
