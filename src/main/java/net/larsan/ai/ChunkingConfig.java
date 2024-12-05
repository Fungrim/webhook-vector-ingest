package net.larsan.ai;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@ConfigMapping(prefix = "chunking")
public interface ChunkingConfig {

    @Positive
    @WithDefault("512")
    int maxSize();

    @PositiveOrZero
    @WithDefault("0")
    int maxOverlap();

}
