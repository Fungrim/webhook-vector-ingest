package net.larsan.ai.embed;

import java.util.Optional;

import com.google.common.base.Strings;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "pinecone")
public interface PineconeConfig {

    Optional<String> apiKey();

    Optional<String> index();

    @WithDefault("true")
    boolean enabled();

    public default boolean isLegal() {
        return !Strings.isNullOrEmpty(apiKey().orElse(null))
                && !Strings.isNullOrEmpty(index().orElse(null));
    }
}
