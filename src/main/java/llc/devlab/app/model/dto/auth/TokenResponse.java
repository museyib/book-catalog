package llc.devlab.app.model.dto.auth;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds,
        AccountResponse account
) {
}
