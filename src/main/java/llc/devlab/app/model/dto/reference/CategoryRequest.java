package llc.devlab.app.model.dto.reference;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name is required.")
        @Size(max = 120, message = "Category name must be at most 120 characters.")
        String name,
        @Size(max = 1000, message = "Description must be at most 1000 characters.")
        String description
) {
}
