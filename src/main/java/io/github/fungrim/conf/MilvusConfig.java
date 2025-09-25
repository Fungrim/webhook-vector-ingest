package io.github.fungrim.conf;

import java.util.Optional;

import com.google.common.base.Strings;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "milvus")
public interface MilvusConfig {

    Optional<String> token();

    Optional<String> host();

    Optional<Integer> port();

    @WithDefault("true")
    boolean enabled();

    @WithDefault("false")
    boolean secure();

    @WithDefault("true")
    boolean jsonMetadata();

    @WithDefault("metadata")
    String jsonMetadataFieldName();

    Optional<String> database();

    Optional<String> collection();

    @WithDefault("vector")
    String vectorFieldName();

    public default boolean isLegal() {
        return !Strings.isNullOrEmpty(token().orElse(null))
                && !Strings.isNullOrEmpty(database().orElse(null));
    }
}
