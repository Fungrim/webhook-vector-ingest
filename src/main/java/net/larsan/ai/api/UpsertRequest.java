package net.larsan.ai.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import dev.langchain4j.data.embedding.Embedding;
import jakarta.validation.constraints.NotBlank;
import net.larsan.ai.embed.Upsert;

public record UpsertRequest(
        @NotBlank String id,
        Optional<String> namespace,
        Optional<List<MetadataField>> metadata,
        @NotBlank String mimeType,
        Optional<String> encoding,
        @NotBlank String content) {

    public Upsert toUpsert(Embedding e) {
        return new Upsert(id, namespace.orElse(null), metadata.orElse(Collections.emptyList()), e.vectorAsList());
    }
}
