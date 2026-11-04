package llc.devlab.app.model.dto.auth;

import java.time.Instant;
import llc.devlab.app.model.enums.Role;

public record AccountResponse(
        Long id,
        String email,
        Role role,
        Instant createdAt
) {
}
