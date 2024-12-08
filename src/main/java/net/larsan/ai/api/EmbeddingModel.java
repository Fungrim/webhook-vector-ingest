package net.larsan.ai.api;

import jakarta.validation.constraints.NotBlank;

public record EmbeddingModel(@NotBlank String provider, @NotBlank String name) {

}
