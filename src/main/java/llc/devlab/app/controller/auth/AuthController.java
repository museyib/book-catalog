package llc.devlab.app.controller.auth;

import jakarta.validation.Valid;
import llc.devlab.app.model.dto.auth.LoginRequest;
import llc.devlab.app.model.dto.auth.RefreshTokenRequest;
import llc.devlab.app.model.dto.auth.RegisterRequest;
import llc.devlab.app.model.dto.auth.TokenResponse;
import llc.devlab.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }
}
