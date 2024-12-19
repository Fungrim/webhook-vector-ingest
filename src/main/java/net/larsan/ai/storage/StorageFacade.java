package net.larsan.ai.storage;

import java.util.List;
import java.util.stream.Stream;

import net.larsan.ai.api.MetadataField;

@FunctionalInterface
public interface StorageFacade {

    public static record Upsert(String id, String namespace, Stream<MetadataField> metadata, List<Float> values) {
    }

    void upsert(Upsert req);

}