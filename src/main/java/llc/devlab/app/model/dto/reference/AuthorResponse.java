package llc.devlab.app.model.dto.reference;

import java.time.Instant;

public record AuthorResponse(
        Long id,
        String name,
        String biography,
        Instant createdAt,
        Instant updatedAt
) {
}
