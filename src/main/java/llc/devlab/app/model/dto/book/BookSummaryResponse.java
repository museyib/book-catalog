package llc.devlab.app.model.dto.book;

import java.math.BigDecimal;
import java.util.List;

public record BookSummaryResponse(
        Long id,
        String title,
        String isbn,
        Integer publicationYear,
        BigDecimal rating,
        BookCategorySummaryResponse category,
        List<BookAuthorSummaryResponse> authors
) {
}
