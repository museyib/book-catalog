package llc.devlab.app.model.dto.reference;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
}
