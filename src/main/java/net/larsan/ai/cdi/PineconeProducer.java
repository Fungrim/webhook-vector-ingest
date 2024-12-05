package net.larsan.ai.cdi;

import com.google.common.base.Strings;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import net.larsan.ai.embed.Pinecone;
import net.larsan.ai.embed.PineconeConfig;

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
        return new Pinecone(conf);
    }
}
