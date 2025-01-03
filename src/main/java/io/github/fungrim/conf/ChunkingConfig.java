package io.github.fungrim.conf;

import io.github.fungrim.api.ChunkingStrategy;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@ConfigMapping(prefix = "chunking")
public interface ChunkingConfig {

    @WithDefault("WORD")
    ChunkingStrategy strategy();

    Limits limits();

    public interface Limits {

        @Positive
        @WithDefault("512")
        int maxSize();

        @PositiveOrZero
        @WithDefault("0")
        int maxOverlap();

    }
}
