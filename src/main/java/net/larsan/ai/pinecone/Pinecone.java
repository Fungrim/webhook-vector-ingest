package net.larsan.ai.pinecone;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.openapitools.inference.client.ApiException;

import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import io.pinecone.clients.Index;
import io.pinecone.clients.Inference;
import net.larsan.ai.api.MetadataField;
import net.larsan.ai.conf.PineconeConfig;
import net.larsan.ai.embedding.EmbeddingFacade;
import net.larsan.ai.storage.StorageFacade;

public class Pinecone implements StorageFacade, EmbeddingFacade {

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
    public List<Embedding> embed(String model, List<TextSegment> chunks) {
        List<String> texts = chunks.stream().map(c -> c.text()).toList();
        try {
            return infererence.embed(model, Collections.emptyMap(), texts).getData().stream()
                    .map(e -> new Embedding(toFloats(e))).toList();
        } catch (ApiException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("null")
    private float[] toFloats(org.openapitools.inference.client.model.Embedding e) {
        if (e.getValues() == null || e.getValues().size() == 0) {
            return new float[0];
        } else {
            float[] a = new float[e.getValues().size()];
            for (int i = 0; i < a.length; i++) {
                a[i] = e.getValues().get(i).floatValue();
            }
            return a;
        }
    }

    private Struct toStruct(Stream<MetadataField> metadata) {
        Builder b = Struct.newBuilder();
        metadata.forEach(f -> {
            b.putFields(f.name(), f.toProtobufValue());
        });
        return b.build();
    }
}
