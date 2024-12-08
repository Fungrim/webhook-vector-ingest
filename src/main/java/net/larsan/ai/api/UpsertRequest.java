package net.larsan.ai.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import dev.langchain4j.data.embedding.Embedding;
import io.smallrye.common.constraint.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import net.larsan.ai.storage.VectorStorage.Upsert;

public record UpsertRequest(
        @Valid @NotNull EmbeddingModel embedding,
        @Valid @NotNull VectorStorage storage,
        @Valid @NotNull Data data) {

    public static record Data(
            @NotBlank String id,
            @NotBlank String mimeType,
            @NotBlank String content,
            Optional<List<MetadataField>> metadata,
            Optional<String> encoding) {

    }

    public Upsert toUpsert(Embedding e, Optional<String> namespace) {
        return new Upsert(data.id, namespace.orElse(null), data.metadata.orElse(Collections.emptyList()), e.vectorAsList());
    }
}
