package llc.devlab.app.service;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import llc.devlab.app.exception.BadRequestException;
import llc.devlab.app.exception.ConflictException;
import llc.devlab.app.model.dto.auth.AccountResponse;
import llc.devlab.app.model.dto.auth.LoginRequest;
import llc.devlab.app.model.dto.auth.RefreshTokenRequest;
import llc.devlab.app.model.dto.auth.RegisterRequest;
import llc.devlab.app.model.dto.auth.TokenResponse;
import llc.devlab.app.model.entity.Account;
import llc.devlab.app.model.entity.RefreshToken;
import llc.devlab.app.model.enums.Role;
import llc.devlab.app.repository.AccountRepository;
import llc.devlab.app.repository.RefreshTokenRepository;
import llc.devlab.app.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (accountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("An account with that email already exists.");
        }

        Account account = new Account();
        account.setEmail(normalizedEmail);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setRole(Role.USER);
        Account savedAccount = accountRepository.save(account);
        return issueTokens(savedAccount);
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );
        Account account = accountRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Account not found."));
        return issueTokens(account);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        JwtService.ParsedRefreshToken parsed = parseRefreshToken(request.refreshToken());
        RefreshToken existingToken = loadRefreshToken(parsed.tokenId());
        validateRefreshToken(existingToken, parsed.secret());

        existingToken.setLastUsedAt(Instant.now());
        IssuedRefreshToken replacementToken = createRefreshToken(existingToken.getAccount());
        existingToken.setRevokedAt(Instant.now());
        existingToken.setReplacedByTokenId(replacementToken.persistedToken().getId());
        refreshTokenRepository.save(existingToken);

        String accessToken = jwtService.generateAccessToken(existingToken.getAccount());
        return new TokenResponse(
                accessToken,
                replacementToken.rawToken(),
                jwtService.getAccessTokenExpirationSeconds(),
                toAccountResponse(existingToken.getAccount())
        );
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        JwtService.ParsedRefreshToken parsed = parseRefreshToken(request.refreshToken());
        refreshTokenRepository.findById(parsed.tokenId()).ifPresent(token -> {
            validateRefreshToken(token, parsed.secret());
            if (!token.isRevoked()) {
                token.setRevokedAt(Instant.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    private TokenResponse issueTokens(Account account) {
        String accessToken = jwtService.generateAccessToken(account);
        IssuedRefreshToken issuedRefreshToken = createRefreshToken(account);
        return new TokenResponse(
                accessToken,
                issuedRefreshToken.rawToken(),
                jwtService.getAccessTokenExpirationSeconds(),
                toAccountResponse(account)
        );
    }

    private IssuedRefreshToken createRefreshToken(Account account) {
        JwtService.RefreshTokenPayload payload = jwtService.createRefreshTokenPayload();
        RefreshToken persisted = persistRefreshToken(account, payload);
        return new IssuedRefreshToken(persisted, jwtService.formatRefreshToken(payload));
    }

    private RefreshToken persistRefreshToken(Account account, JwtService.RefreshTokenPayload payload) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(payload.tokenId());
        refreshToken.setAccount(account);
        refreshToken.setTokenHash(passwordEncoder.encode(payload.secret()));
        refreshToken.setExpiresAt(payload.expiresAt());
        return refreshTokenRepository.save(refreshToken);
    }

    private JwtService.ParsedRefreshToken parseRefreshToken(String refreshToken) {
        try {
            return jwtService.parseRefreshToken(refreshToken);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Refresh token format is invalid.");
        }
    }

    private RefreshToken loadRefreshToken(UUID tokenId) {
        return refreshTokenRepository.findById(tokenId)
                .orElseThrow(() -> new BadRequestException("Refresh token is invalid."));
    }

    private void validateRefreshToken(RefreshToken refreshToken, String secret) {
        if (refreshToken.isRevoked()) {
            throw new BadRequestException("Refresh token has been revoked.");
        }
        if (refreshToken.isExpired(Instant.now())) {
            throw new BadRequestException("Refresh token has expired.");
        }
        if (!passwordEncoder.matches(secret, refreshToken.getTokenHash())) {
            throw new BadRequestException("Refresh token is invalid.");
        }
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(account.getId(), account.getEmail(), account.getRole(), account.getCreatedAt());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private record IssuedRefreshToken(RefreshToken persistedToken, String rawToken) {
    }
}
