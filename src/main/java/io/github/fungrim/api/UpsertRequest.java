package io.github.fungrim.api;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.google.common.collect.Streams;

import dev.langchain4j.data.embedding.Embedding;
import io.github.fungrim.storage.StorageFacade;
import io.smallrye.common.constraint.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record UpsertRequest(
        Optional<EmbeddingModelSpec> embedding,
        Optional<VectorStorageSpec> storage,
        Optional<ChunkingSpec> chunking,
        @Valid @NotNull Data data) {

    public static record Data(
            Optional<String> id,
            Optional<String> contentType,
            Optional<String> fileName,
            @NotBlank String content,
            Optional<List<MetadataField>> metadata,
            Optional<Encoding> encoding,
            Optional<Boolean> fetchContent) {

    }

    public StorageFacade.Upsert toUpsert(Embedding e, Optional<String> namespace,
            Optional<List<MetadataField>> extraMeta) {
        return new StorageFacade.Upsert(
                data.id.orElse(NanoIdUtils.randomNanoId()),
                namespace.orElse(null),
                Streams.concat(extraMeta.orElse(Collections.emptyList()).stream(),
                        data.metadata.orElse(Collections.emptyList()).stream()),
                e.vectorAsList());
    }
}
