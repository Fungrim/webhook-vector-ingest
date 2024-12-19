package net.larsan.ai.conf;

import java.util.List;
import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "metadata")
public interface MetadataConfig {

    ContentType contentType();

    FileName fileName();

    Html html();

    public interface ContentType {

        @WithDefault("content-type")
        String name();

        @WithDefault("true")
        boolean enabled();

    }

    public interface FileName {

        @WithDefault("file-name")
        String name();

        @WithDefault("true")
        boolean enabled();

    }

    public interface Html {

        Optional<List<String>> includeHeaders();
    }
}
