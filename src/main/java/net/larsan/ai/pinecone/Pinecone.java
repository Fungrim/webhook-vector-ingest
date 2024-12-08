package net.larsan.ai.pinecone;

import java.util.List;

import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;

import io.pinecone.clients.Index;
import io.pinecone.clients.Inference;
import net.larsan.ai.api.MetadataField;
import net.larsan.ai.embed.Database;

public class Pinecone implements Database {

    private final io.pinecone.clients.Pinecone client;
    private final Index index;
    private Inference interference;

    public Pinecone(PineconeConfig conf) {
        client = new io.pinecone.clients.Pinecone.Builder(conf.apiKey().get()).build();
        index = client.getIndexConnection(conf.index().get());
        interference = client.getInferenceClient();
    }

    public void upsert(Upsert req) {
        index.upsert(req.id(), req.values(), null, null, toStruct(req.metadata()), req.namespace());
    }

    private Struct toStruct(List<MetadataField> metadata) {
        Builder b = Struct.newBuilder();
        metadata.forEach(f -> {
            b.putFields(f.name(), f.toProtobufValue());
        });
        return b.build();
    }
}
