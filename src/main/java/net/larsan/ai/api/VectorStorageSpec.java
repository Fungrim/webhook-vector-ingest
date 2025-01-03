package net.larsan.ai.api;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;

public record VectorStorageSpec(@NotBlank String provider, Optional<String> namespace) {

}
