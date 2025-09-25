package io.github.fungrim.conf;

import java.util.Optional;

import io.github.fungrim.api.VectorStorageSpec;
import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.NotBlank;

@ConfigMapping(prefix = "storage")
public interface VectorStorageConfig {

    Optional<Storage> defaultStorage();

    public static interface Storage {

        @NotBlank
        String provider();

        Optional<String> namespace();

        public default VectorStorageSpec toVectorStorageSpec() {
            return new VectorStorageSpec(
                Optional.of(provider()),
                Optional.empty(),
                namespace());
        }
    }
}
