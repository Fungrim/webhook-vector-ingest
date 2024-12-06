package net.larsan.ai.embed;

import java.util.Collections;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.vector.request.UpsertReq;

public class Milvus implements Database {

    private final ConnectConfig connectConfig;
    private final MilvusClientV2 client;
    private final MilvusConfig conf;
    private final Gson gson;

    public Milvus(MilvusConfig conf, Gson gson) {
        this.conf = conf;
        this.gson = gson;
        connectConfig = ConnectConfig.builder()
                .uri(conf.uri().get())
                .token(conf.accessToken().get())
                .secure(conf.secure())
                .dbName(conf.database().get())
                .build();
        client = new MilvusClientV2(connectConfig);
    }

    @Override
    public void upsert(Upsert req) {
        JsonObject o = new JsonObject();
        req.metadata().forEach(f -> {
            f.setAsGsonProperty(o);
        });
        o.add(conf.vectorFieldName(), gson.toJsonTree(req.values()));
        o.addProperty("id", req.id());
        client.upsert(UpsertReq.builder()
                .collectionName(conf.collection().get())
                .partitionName(req.namespace())
                .data(Collections.singletonList(o))
                .build());
    }
}
