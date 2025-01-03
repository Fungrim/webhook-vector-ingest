package io.github.fungrim.cdi;

import com.google.common.base.Strings;

import io.github.fungrim.conf.PineconeConfig;
import io.github.fungrim.pinecone.Pinecone;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

public class PineconeProducer {

    @Produces
    @Singleton
    Pinecone pinecone(PineconeConfig conf) {
        if (!conf.enabled()) {
            throw new IllegalStateException("Pinecone not enabled");
        }
        if (Strings.isNullOrEmpty(conf.apiKey().orElse(null))) {
            throw new IllegalStateException("Pinecone api key is empty");
        }
        if (Strings.isNullOrEmpty(conf.uri().orElse(null))) {
            throw new IllegalStateException("Pinecone uri is empty");
        }
        return new Pinecone(conf);
    }
}
