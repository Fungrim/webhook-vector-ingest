package net.larsan.ai;

import java.util.Optional;

import com.google.common.base.Strings;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "openai")
public interface OpenAIConfig {

    Optional<String> apiKey();

    Optional<String> url();

    public default boolean isLegal() {
        return !Strings.isNullOrEmpty(apiKey().orElse(null));
    }
}
