package llc.devlab.app.service;

import java.util.List;
import java.util.Map;
import llc.devlab.app.exception.BadRequestException;
import org.springframework.data.domain.Sort;

public final class SortParser {

    private static final Map<String, String> ALLOWED_FIELDS = Map.of(
            "title", "title",
            "publicationYear", "publicationYear",
            "rating", "rating"
    );

    private SortParser() {
    }

    public static Sort parse(String sortExpression) {
        if (sortExpression == null || sortExpression.isBlank()) {
            return Sort.by(Sort.Order.asc("title"));
        }

        List<Sort.Order> orders = java.util.Arrays.stream(sortExpression.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(SortParser::parseOrder)
                .toList();

        if (orders.isEmpty()) {
            throw new BadRequestException("Sort expression cannot be empty.");
        }

        return Sort.by(orders);
    }

    private static Sort.Order parseOrder(String token) {
        String[] parts = token.split(":", 2);
        if (parts.length != 2) {
            throw new BadRequestException(
                    "Invalid sort token '%s'. Use field:direction.".formatted(token)
            );
        }

        String requestedField = parts[0].trim();
        String direction = parts[1].trim().toLowerCase();
        String field = ALLOWED_FIELDS.get(requestedField);
        if (field == null) {
            throw new BadRequestException(
                    "Unsupported sort field '%s'. Allowed fields: title, publicationYear, rating."
                            .formatted(requestedField)
            );
        }
        return switch (direction) {
            case "asc" -> Sort.Order.asc(field);
            case "desc" -> Sort.Order.desc(field);
            default -> throw new BadRequestException(
                    "Unsupported sort direction '%s' for field '%s'. Use asc or desc."
                            .formatted(direction, requestedField)
            );
        };
    }
}
