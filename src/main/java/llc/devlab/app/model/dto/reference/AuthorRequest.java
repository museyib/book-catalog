package llc.devlab.app.model.dto.reference;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequest(
        @NotBlank(message = "Author name is required.")
        @Size(max = 200, message = "Author name must be at most 200 characters.")
        String name,
        @Size(max = 4000, message = "Biography must be at most 4000 characters.")
        String biography
) {
}
