package llc.devlab.app.model.dto.book;

import java.math.BigDecimal;
import java.util.Set;

public record BookUpdateRequest(
        String title,
        String isbn,
        Integer publicationYear,
        BigDecimal rating,
        Long categoryId,
        Set<Long> authors
) {
}
