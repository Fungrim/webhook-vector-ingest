package net.larsan.ai.conf;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import net.larsan.ai.api.ChunkingStrategy;

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
