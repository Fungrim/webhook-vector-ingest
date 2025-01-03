package net.larsan.ai.conf;

import java.util.List;
import java.util.Optional;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "security")
public interface SecurityConfig {

    Optional<List<String>> apiKeys();

}
