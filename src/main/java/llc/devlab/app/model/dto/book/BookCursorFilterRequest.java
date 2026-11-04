package llc.devlab.app.model.dto.book;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookCursorFilterRequest {

    private Long cursor;

    private String title;

    private String authorName;

    private String category;

    @Min(value = 1450, message = "publishedYearFrom must be 1450 or later.")
    @Max(value = 2100, message = "publishedYearFrom must be 2100 or earlier.")
    private Integer publishedYearFrom;

    @Min(value = 1450, message = "publishedYearTo must be 1450 or later.")
    @Max(value = 2100, message = "publishedYearTo must be 2100 or earlier.")
    private Integer publishedYearTo;

    @DecimalMin(value = "0.0", message = "minRating must be at least 0.0.")
    @DecimalMax(value = "5.0", message = "minRating must be at most 5.0.")
    private java.math.BigDecimal minRating;

    @AssertTrue(message = "publishedYearFrom must be less than or equal to publishedYearTo.")
    public boolean isValidYearRange() {
        return publishedYearFrom == null || publishedYearTo == null || publishedYearFrom <= publishedYearTo;
    }
}
