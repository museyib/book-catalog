package llc.devlab.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import llc.devlab.app.config.AppProperties;
import llc.devlab.app.model.entity.Account;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AppProperties properties;
    private final Key signingKey;

    public JwtService(AppProperties properties) {
        this.properties = properties;
        byte[] keyBytes = properties.security().secret().getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Account account) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(properties.security().accessTokenExpirationSeconds());
        return Jwts.builder()
                .issuer(properties.security().issuer())
                .subject(account.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .claim("uid", account.getId())
                .claim("role", account.getRole().name())
                .signWith(signingKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(signingKey.getEncoded()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, AccountPrincipal principal) {
        Claims claims = parseClaims(token);
        return principal.getUsername().equalsIgnoreCase(claims.getSubject())
                && claims.getExpiration().toInstant().isAfter(Instant.now())
                && properties.security().issuer().equals(claims.getIssuer());
    }

    public long getAccessTokenExpirationSeconds() {
        return properties.security().accessTokenExpirationSeconds();
    }

    public RefreshTokenPayload createRefreshTokenPayload() {
        UUID tokenId = UUID.randomUUID();
        byte[] buffer = new byte[32];
        SECURE_RANDOM.nextBytes(buffer);
        String secret = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(buffer);
        Instant expiresAt = Instant.now().plusSeconds(properties.security().refreshTokenExpirationSeconds());
        return new RefreshTokenPayload(tokenId, secret, expiresAt);
    }

    public ParsedRefreshToken parseRefreshToken(String refreshToken) {
        String[] parts = refreshToken.split("\\.", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Refresh token format is invalid.");
        }
        return new ParsedRefreshToken(UUID.fromString(parts[0]), parts[1]);
    }

    public String formatRefreshToken(RefreshTokenPayload payload) {
        return payload.tokenId() + "." + payload.secret();
    }

    public record RefreshTokenPayload(UUID tokenId, String secret, Instant expiresAt) {
    }

    public record ParsedRefreshToken(UUID tokenId, String secret) {
    }
}
