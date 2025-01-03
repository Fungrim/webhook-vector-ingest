package io.github.fungrim.cdi;

import com.google.common.base.Strings;
import com.google.gson.Gson;

import io.github.fungrim.conf.MilvusConfig;
import io.github.fungrim.milvus.Milvus;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

public class MilvusProducer {

    @Produces
    @Singleton
    Milvus milvus(MilvusConfig conf, Gson gson) {
        if (!conf.enabled()) {
            throw new IllegalStateException("Milvus not enabled");
        }
        if (Strings.isNullOrEmpty(conf.token().orElse(null))) {
            throw new IllegalStateException("Milvus access token is empty");
        }
        if (Strings.isNullOrEmpty(conf.uri().orElse(null))) {
            throw new IllegalStateException("Milvus uri is empty");
        }
        if (Strings.isNullOrEmpty(conf.database().orElse(null))) {
            throw new IllegalStateException("Milvus database name is empty");
        }
        if (Strings.isNullOrEmpty(conf.collection().orElse(null))) {
            throw new IllegalStateException("Milvus collection name is empty");
        }
        return new Milvus(conf, gson);
    }
}
