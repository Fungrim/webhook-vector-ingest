package net.larsan.ai.embed;

import java.util.List;

import net.larsan.ai.api.MetadataField;

@FunctionalInterface
public interface Database {

    public static record Upsert(String id, String namespace, List<MetadataField> metadata, List<Float> values) {

    }

    void upsert(Upsert req);

}