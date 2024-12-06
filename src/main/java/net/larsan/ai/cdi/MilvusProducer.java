package net.larsan.ai.cdi;

import com.google.common.base.Strings;
import com.google.gson.Gson;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import net.larsan.ai.embed.Milvus;
import net.larsan.ai.embed.MilvusConfig;

public class MilvusProducer {

    @Produces
    @Singleton
    Milvus milvus(MilvusConfig conf, Gson gson) {
        if (!conf.enabled()) {
            throw new IllegalStateException("Milvus not enabled");
        }
        if (Strings.isNullOrEmpty(conf.accessToken().orElse(null))) {
            throw new IllegalStateException("Milvus access token is empty");
        }
        if (Strings.isNullOrEmpty(conf.uri().orElse(null))) {
            throw new IllegalStateException("Milvus uri is empty");
        }
        if (Strings.isNullOrEmpty(conf.database().orElse(null))) {
            throw new IllegalStateException("Milvus db name is empty");
        }
        return new Milvus(conf, gson);
    }
}
