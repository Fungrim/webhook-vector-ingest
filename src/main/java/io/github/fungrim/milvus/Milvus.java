package io.github.fungrim.milvus;

import java.util.Collections;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.github.fungrim.conf.MilvusConfig;
import io.github.fungrim.storage.StorageFacade;
import io.github.fungrim.util.ProblemFactory;
import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.dml.UpsertParam;
import io.quarkus.logging.Log;
import jakarta.ws.rs.core.Response;

public class Milvus implements StorageFacade {

    private ConnectParam connectConfig;
    private final MilvusServiceClient client;
    private final MilvusConfig conf;
    private final Gson gson;

    public Milvus(MilvusConfig conf, Gson gson) {
        this.conf = conf;
        this.gson = gson;
        connectConfig = ConnectParam.newBuilder()
                .withHost(conf.host().orElse("localhost"))
                .withPort(conf.port().orElse(19530))
                .withDatabaseName(conf.database().get())
                .withToken(conf.token().get())
                .build();
        client = new MilvusServiceClient(connectConfig);
    }

    @Override
    public void upsert(Upsert req) {
        if(conf.collection().isEmpty() && req.collection().isEmpty()) {
            throw new IllegalStateException("Milvus collection not configured nor specified in request");
        }
        JsonObject o = toRow(req);
        UpsertParam.Builder param = toRequestBuilder(req, o);
        try {
            client.upsert(param.build());
        } catch (Exception e) {
            Log.error("Failed to upsert document, will pass on as an internal error", e);
            throw ProblemFactory.problem(Response.Status.INTERNAL_SERVER_ERROR, "Milvus exception", 0, e.getMessage());
        }
    }

    private UpsertParam.Builder toRequestBuilder(Upsert req, JsonObject o) {
        UpsertParam.Builder param = UpsertParam.newBuilder()
                .withRows(Collections.singletonList(o));
        if (req.namespace().isPresent()) {
            param.withPartitionName(req.namespace().get());
        }
        if(req.collection().isPresent()) {
            param.withCollectionName(req.collection().get());
        } else {
            param.withCollectionName(conf.collection().get());
        }
        return param;
    }

    private JsonObject toRow(Upsert req) {
        JsonObject o = new JsonObject();
        if (conf.jsonMetadata()) {
            JsonObject meta = new JsonObject();
            req.metadata().forEach(f -> {
                f.setAsGsonProperty(meta);
            });
            o.add(conf.jsonMetadataFieldName(), meta);
        } else {
            req.metadata().forEach(f -> {
                f.setAsGsonProperty(o);
            });
        }
        o.add(conf.vectorFieldName(), gson.toJsonTree(req.values()));
        o.addProperty("id", req.id());
        return o;
    }
}
