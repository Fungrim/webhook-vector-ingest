package net.larsan.ai.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record ChunkingSpec(@NotNull ChunkingStrategy strategy, @NotNull @Valid Limits limits) {

    public record Limits(@Positive int maxSize, @PositiveOrZero int maxOverlap) {
    }
}
