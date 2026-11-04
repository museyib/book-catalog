package llc.devlab.app.model.dto.book;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record BookResponse(
        Long id,
        String title,
        String isbn,
        String description,
        Integer publicationYear,
        BigDecimal rating,
        BookCategorySummaryResponse category,
        List<BookAuthorSummaryResponse> authors,
        Instant createdAt,
        Instant updatedAt
) {
}
