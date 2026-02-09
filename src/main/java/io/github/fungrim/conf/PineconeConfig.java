package io.github.fungrim.conf;

import java.time.Duration;
import java.util.Optional;

import com.google.common.base.Strings;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.validation.constraints.Positive;

@ConfigMapping(prefix = "pinecone")
public interface PineconeConfig {

    Optional<String> apiKey();

    Optional<String> index();

    Optional<String> uri();

    @WithDefault("true")
    boolean enabled();

    @WithDefault("96")
    int embeddingBatchSize();

    Retry rateLimitRetry();

    public default boolean isLegal() {
        return !Strings.isNullOrEmpty(apiKey().orElse(null))
                && !Strings.isNullOrEmpty(index().orElse(null))
                && !Strings.isNullOrEmpty(uri().orElse(null));
    }

    public interface Retry {

        @WithDefault("false")
        boolean enabled();

        @WithDefault("PT15S")
        Duration baseInterval();

        @WithDefault("true")
        boolean exponantionalBackoff();
    
        @Positive
        @WithDefault("5")
        int maxAttempts();
    }
}
