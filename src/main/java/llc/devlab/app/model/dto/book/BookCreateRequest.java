package llc.devlab.app.model.dto.book;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Set;

public record BookCreateRequest(
        @NotBlank(message = "Book title is required.")
        @Size(max = 200, message = "Book title must be at most 200 characters.")
        String title,
        @NotBlank(message = "ISBN is required.")
        @Size(max = 20, message = "ISBN must be at most 20 characters.")
        String isbn,
        @Size(max = 4000, message = "Description must be at most 4000 characters.")
        String description,
        @NotNull(message = "Publication year is required.")
        @Min(value = 1450, message = "Publication year must be 1450 or later.")
        @Max(value = 2100, message = "Publication year must be 2100 or earlier.")
        Integer publicationYear,
        @NotNull(message = "Rating is required.")
        @DecimalMin(value = "0.0", message = "Rating must be at least 0.0.")
        @DecimalMax(value = "5.0", message = "Rating must be at most 5.0.")
        BigDecimal rating,
        @NotNull(message = "Category id is required.")
        Long categoryId,
        Set<Long> authors
) {
}
