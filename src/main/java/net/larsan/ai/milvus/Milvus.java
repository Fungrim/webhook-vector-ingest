package net.larsan.ai.milvus;

import java.util.Collections;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.utility.request.FlushReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.request.UpsertReq.UpsertReqBuilder;
import net.larsan.ai.storage.VectorStorage;

public class Milvus implements VectorStorage {

    private final ConnectConfig connectConfig;
    private final MilvusClientV2 client;
    private final MilvusConfig conf;
    private final Gson gson;

    public Milvus(MilvusConfig conf, Gson gson) {
        this.conf = conf;
        this.gson = gson;
        connectConfig = ConnectConfig.builder()
                .uri(conf.uri().get())
                .token(conf.token().get())
                .secure(conf.secure())
                .dbName(conf.database().get())
                .build();
        client = new MilvusClientV2(connectConfig);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void upsert(Upsert req) {
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
        UpsertReqBuilder builder = UpsertReq.builder()
                .collectionName(conf.collection().get())
                .data(Collections.singletonList(o));
        if (!Strings.isNullOrEmpty(req.namespace())) {
            builder.partitionName(req.namespace());
        }
        client.upsert(builder.build());
        client.flush(FlushReq.builder().collectionNames(Collections.singletonList(conf.collection().get())).build());
    }
}
