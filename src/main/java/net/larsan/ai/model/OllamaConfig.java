package net.larsan.ai.model;

import java.util.Optional;

import com.google.common.base.Strings;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "ollama")
public interface OllamaConfig {

    Optional<String> uri();

    public default boolean isLegal() {
        return !Strings.isNullOrEmpty(uri().orElse(null));
    }
}
