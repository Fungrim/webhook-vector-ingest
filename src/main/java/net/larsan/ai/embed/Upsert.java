package net.larsan.ai.embed;

import java.util.List;

import net.larsan.ai.api.MetadataField;

public record Upsert(String id, String namespace, List<MetadataField> metadata, List<Float> values) {

}
