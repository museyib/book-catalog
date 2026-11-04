package llc.devlab.app.mapper;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import llc.devlab.app.model.dto.book.*;
import llc.devlab.app.model.entity.Author;
import llc.devlab.app.model.entity.Book;
import llc.devlab.app.model.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public abstract class BookMapper {

    @PersistenceContext
    private EntityManager entityManager;

    public BookSummaryResponse toSummary(Book book) {
        return new BookSummaryResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getPublicationYear(),
                book.getRating(),
                new BookCategorySummaryResponse(book.getCategory().getId(), book.getCategory().getName()),
                mapAuthors(book)
        );
    }

    public BookResponse toResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getDescription(),
                book.getPublicationYear(),
                book.getRating(),
                new BookCategorySummaryResponse(book.getCategory().getId(), book.getCategory().getName()),
                mapAuthors(book),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }

    public List<BookAuthorSummaryResponse> mapAuthors(Book book) {
        return book.getAuthors()
                .stream()
                .sorted(Comparator.comparing(Author::getName))
                .map(author -> new BookAuthorSummaryResponse(author.getId(), author.getName()))
                .toList();
    }

    @Mapping(target = "category", source = "categoryId")
    public abstract Book toEntity(BookCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "category", source = "categoryId")
    public abstract void toEntity(BookUpdateRequest request, @MappingTarget Book book);

    Set<Author> toAuthorSet(Set<Long> labels) {
        if (labels == null) {
            return null;
        }
        return labels.stream()
                .map(labelId -> entityManager.getReference(Author.class, labelId))
                .collect(Collectors.toSet());
    }

    Category toCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return entityManager.getReference(Category.class, categoryId);
    }
}
