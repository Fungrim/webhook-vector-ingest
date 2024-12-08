package net.larsan.ai.conf;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotBlank;

@ConfigMapping(prefix = "storage")
public interface VectorStorageConfig {

    Optional<Storage> defaultStorage();

    public static interface Storage {

        @NotBlank
        String provider();

        Optional<String> namespace();

    }
}
