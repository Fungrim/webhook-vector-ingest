package io.github.fungrim.storage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.fungrim.api.MetadataField;
import jakarta.validation.constraints.NotBlank;

@FunctionalInterface
public interface StorageFacade {

    public static record Upsert(@NotBlank String id, Optional<String> collection, Optional<String> namespace, Stream<MetadataField> metadata, List<Float> values) {
    }

    void upsert(Upsert req);

}