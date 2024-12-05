package net.larsan.ai.embed;

import java.util.Optional;

import com.google.common.base.Strings;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "milvui")
public interface MilvusConfig {

    Optional<String> accessToken();

    Optional<String> uri();

    @WithDefault("true")
    boolean enabled();

    @WithDefault("true")
    boolean secure();

    Optional<String> dbName();

    @WithDefault("vector")
    String vectorFieldName();

    public default boolean isLegal() {
        return !Strings.isNullOrEmpty(accessToken().orElse(null))
                && !Strings.isNullOrEmpty(uri().orElse(null))
                && !Strings.isNullOrEmpty(dbName().orElse(null));
    }
}
