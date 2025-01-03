package net.larsan.ai;

import org.jboss.resteasy.reactive.server.ServerRequestFilter;

import com.google.common.base.Strings;

import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import net.larsan.ai.conf.SecurityConfig;

@Singleton
public class ApiKeyFilter {

    @Inject
    Instance<SecurityConfig> securityConfig;

    @ServerRequestFilter
    public void filter(ContainerRequestContext con) {
        if (!securityConfig.get().apiKeys().isEmpty()) {
            String apiKey = con.getHeaderString("x-api-key");
            if (Strings.isNullOrEmpty(apiKey)) {
                apiKey = con.getUriInfo().getQueryParameters().getFirst("x-api-key");
            }
            if (Strings.isNullOrEmpty(apiKey) || !securityConfig.get().apiKeys().get().contains(apiKey)) {
                throw new UnauthorizedException();
            }
        }
    }
}
