package io.github.fungrim.conf;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.OptionalInt;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;
import io.smallrye.config.ConfigValue;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.source.yaml.YamlConfigSource;

public class FileConfigSourceFactory implements ConfigSourceFactory {

    @Override
    public Iterable<ConfigSource> getConfigSources(ConfigSourceContext context) {
        ConfigValue value = context.getValue("ingest.config");
        if (value == null || value.getValue() == null || value.getValue().isEmpty()) {
            return Collections.emptyList();
        } else {
            File f = new File(value.getValue());
            if (!f.exists()) {
                throw new IllegalArgumentException("File " + f.getAbsolutePath() + " does not exist");
            }
            if (f.isDirectory()) {
                throw new IllegalArgumentException("File " + f.getAbsolutePath() + " is a directory");
            }
            return tryParse(f);
        }
    }

    @SuppressWarnings("deprecation")
    private Iterable<ConfigSource> tryParse(File f) {
        try {
            if (isYaml(f.getName())) {
                return Collections.singletonList(new YamlConfigSource(f.toURL()));
            } else if (isProperties(f.getName())) {
                return Collections.singletonList(new PropertiesConfigSource(f.toURL()));
            } else {
                throw new IllegalArgumentException("File " + f.getAbsolutePath() + " is not a YAML or properties file");
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean isProperties(String value) {
        String s = value.toLowerCase();
        return s.endsWith(".properties") || s.endsWith(".props");
    }

    private boolean isYaml(String value) {
        String s = value.toLowerCase();
        return s.endsWith(".yaml") || s.endsWith(".yml");
    }

    @Override
    public OptionalInt getPriority() {
        return OptionalInt.of(290);
    }
}
