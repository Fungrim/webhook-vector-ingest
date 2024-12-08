package net.larsan.ai;

import java.util.Optional;

import com.google.common.base.Strings;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "ollama")
public interface OllamaConfig {

    Optional<String> uri();

    @WithDefault("true")
    boolean enabled();

    public default boolean isLegal() {
        return !Strings.isNullOrEmpty(uri().orElse(null));
    }
}
