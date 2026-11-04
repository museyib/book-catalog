package llc.devlab.app.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Pagination pagination,
        Seed seed,
        Security security
) {

    public record Pagination(
            @Min(1)
            @Max(500)
            int maxPageSize
    ) {
    }

    public record Seed(
            boolean enabled
    ) {
    }

    public record Security(
            @NotBlank
            String issuer,
            @Min(60)
            long accessTokenExpirationSeconds,
            @Min(300)
            long refreshTokenExpirationSeconds,
            @NotBlank
            String secret
    ) {
    }
}
