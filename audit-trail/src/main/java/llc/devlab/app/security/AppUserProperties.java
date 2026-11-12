package llc.devlab.app.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppUserProperties(String username, String password) {
}
