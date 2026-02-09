package io.github.fungrim.pinecone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.openapitools.inference.client.ApiException;

import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import io.github.fungrim.api.MetadataField;
import io.github.fungrim.conf.PineconeConfig;
import io.github.fungrim.embedding.EmbeddingFacade;
import io.github.fungrim.storage.StorageFacade;
import io.github.fungrim.util.OpenApiException;
import io.pinecone.clients.Index;
import io.pinecone.clients.Inference;

public class Pinecone implements StorageFacade, EmbeddingFacade {

    private final io.pinecone.clients.Pinecone client;
    private final Index index;
    private Inference infererence;
    private final int batchSize;

    public Pinecone(PineconeConfig conf) {
        batchSize = conf.embeddingBatchSize();
        client = new io.pinecone.clients.Pinecone.Builder(conf.apiKey().get()).build();
        index = client.getIndexConnection(conf.index().get());
        infererence = client.getInferenceClient();
    }

    public void upsert(Upsert req) {
        index.upsert(req.id(), req.values(), null, null, toStruct(req.metadata()), req.namespace().orElse(null));
    }

    @Override
    public List<Embedding> embed(String model, List<TextSegment> chunks) {
        List<String> texts = chunks.stream().map(c -> c.text()).toList();
        List<Embedding> allEmbeddings = new ArrayList<>();
        
        for (int i = 0; i < texts.size(); i += batchSize) {
            int end = Math.min(i + batchSize, texts.size());
            List<String> batch = texts.subList(i, end);
            
            try {
                List<Embedding> batchEmbeddings = infererence.embed(model, 
                        Collections.singletonMap("input_type", "passage"), batch)
                        .getData().stream()
                        .map(e -> new Embedding(toFloats(e)))
                        .toList();
                allEmbeddings.addAll(batchEmbeddings);
            } catch (ApiException e) {
                throw OpenApiException.fromException(e).toHttpProblem("Pinecone");
            }
        }
        
        return allEmbeddings;
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
            b.putFields(f.key(), f.toProtobufValue());
        });
        return b.build();
    }
}
