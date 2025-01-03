package net.larsan.ai.api;

import jakarta.validation.constraints.NotBlank;

public record EmbeddingModelSpec(@NotBlank String provider, @NotBlank String name) {

}
