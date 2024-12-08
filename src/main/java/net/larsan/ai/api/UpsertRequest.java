package net.larsan.ai.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import dev.langchain4j.data.embedding.Embedding;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import net.larsan.ai.embed.Database;
import net.larsan.ai.embed.Database.Upsert;

public record UpsertRequest(
        @NotBlank String provider,
        @NotBlank String model,
        @NotBlank String database,
        @NotNull Data data) {

    public static record Data(
            @NotBlank String id,
            Optional<String> namespace,
            Optional<List<MetadataField>> metadata,
            @NotBlank String mimeType,
            Optional<String> encoding,
            @NotBlank String content) {

    }

    public Database.Upsert toUpsert(Embedding e) {
        return new Upsert(data.id, data.namespace.orElse(null), data.metadata.orElse(Collections.emptyList()), e.vectorAsList());
    }
}
