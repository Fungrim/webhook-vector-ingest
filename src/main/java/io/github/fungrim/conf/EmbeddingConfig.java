package io.github.fungrim.conf;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotBlank;

@ConfigMapping(prefix = "embedding")
public interface EmbeddingConfig {

    Optional<Model> defaultModel();

    public static interface Model {

        @NotBlank
        String provider();

        @NotBlank
        String name();
    }
}
