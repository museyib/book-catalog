package llc.devlab.app.service;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import llc.devlab.app.model.dto.book.BookCursorFilterRequest;
import llc.devlab.app.model.entity.Author;
import llc.devlab.app.model.entity.Book;
import llc.devlab.app.model.entity.Category;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class BookCursorSpecifications {

    private BookCursorSpecifications() {
    }

    public static Specification<Book> fromFilter(BookCursorFilterRequest filter) {
        return (root, query, builder) -> {
            query.distinct(true);
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getCursor() != null) {
                predicates.add(builder.greaterThan(root.get("id"), filter.getCursor()));
            }

            if (hasText(filter.getTitle())) {
                predicates.add(builder.like(
                        builder.lower(root.get("title")),
                        "%" + filter.getTitle().trim().toLowerCase() + "%"
                ));
            }

            if (hasText(filter.getAuthorName())) {
                Join<Book, Author> authorJoin = root.join("authors", JoinType.LEFT);
                predicates.add(builder.like(
                        builder.lower(authorJoin.get("name")),
                        "%" + filter.getAuthorName().trim().toLowerCase() + "%"
                ));
            }

            if (hasText(filter.getCategory())) {
                Join<Book, Category> categoryJoin = root.join("category", JoinType.INNER);
                predicates.add(builder.equal(
                        builder.lower(categoryJoin.get("name")),
                        filter.getCategory().trim().toLowerCase()
                ));
            }

            if (filter.getPublishedYearFrom() != null) {
                predicates.add(builder.greaterThanOrEqualTo(
                        root.get("publicationYear"),
                        filter.getPublishedYearFrom()
                ));
            }

            if (filter.getPublishedYearTo() != null) {
                predicates.add(builder.lessThanOrEqualTo(
                        root.get("publicationYear"),
                        filter.getPublishedYearTo()
                ));
            }

            if (filter.getMinRating() != null) {
                predicates.add(builder.greaterThanOrEqualTo(
                        root.get("rating"),
                        filter.getMinRating()
                ));
            }

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
