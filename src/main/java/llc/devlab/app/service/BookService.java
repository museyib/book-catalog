package llc.devlab.app.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import llc.devlab.app.Auditable;
import llc.devlab.app.RiskLevel;
import llc.devlab.app.config.AppProperties;
import llc.devlab.app.exception.BadRequestException;
import llc.devlab.app.exception.ConflictException;
import llc.devlab.app.exception.ResourceNotFoundException;
import llc.devlab.app.mapper.BookMapper;
import llc.devlab.app.model.dto.book.*;
import llc.devlab.app.model.dto.common.CursorResponse;
import llc.devlab.app.model.dto.common.PageResponse;
import llc.devlab.app.model.entity.Book;
import llc.devlab.app.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static llc.devlab.app.AuditAction.*;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final AppProperties properties;

    @Auditable(
            action = READ,
            targetType = "Books",
            targetId = "",
            riskLevel = RiskLevel.LOW
    )
    public PageResponse<BookSummaryResponse> list(int page, int size, String sort, BookFilterRequest request) {
        Pageable pageable = createPageable(page, size, sort);
        Page<Book> rawPage = bookRepository.findAll(BookSpecifications.fromFilter(request), pageable);
        if (rawPage.isEmpty()) {
            return PageResponse.of(List.of(), rawPage);
        }

        List<Long> ids = rawPage.getContent().stream().map(Book::getId).toList();
        Map<Long, Book> fetchedBooks = new LinkedHashMap<>();
        bookRepository.findAllWithAuthorsAndCategoryByIdIn(ids)
                .forEach(book -> fetchedBooks.put(book.getId(), book));

        List<BookSummaryResponse> content = ids.stream()
                .map(fetchedBooks::get)
                .filter(Objects::nonNull)
                .map(bookMapper::toSummary)
                .toList();

        return PageResponse.of(content, rawPage);
    }

    @Auditable(
            action = READ,
            targetType = "Books",
            targetId = "",
            riskLevel = RiskLevel.LOW
    )
    public CursorResponse<BookSummaryResponse> list(int size, String sort, BookCursorFilterRequest request) {
        List<Book> result = bookRepository.findBy(BookCursorSpecifications.fromFilter(request),
                spec -> spec
                        .project("authors", "category")
                        .sortBy(SortParser.parse(sort))
                        .stream().toList())
                .stream().limit(size + 1).toList();

        boolean hasNextPage = result.size() > size;

        if (hasNextPage) {
            result = result.subList(0, size);
        }

        Long nextCursorId = result.isEmpty() ? null : result.getLast().getId();

        return new CursorResponse<>(result.stream().map(bookMapper::toSummary).toList(), nextCursorId, hasNextPage);
    }

    @Auditable(
            action = READ,
            targetType = "Book",
            targetId = "#result.id()",
            riskLevel = RiskLevel.LOW
    )
    public BookResponse get(Long id) {
        Book book = bookRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id %d was not found.".formatted(id)));
        return bookMapper.toResponse(book);
    }

    @Transactional
    @Auditable(
            action = CREATE,
            targetType = "Book",
            targetId = "#result.id()",
            riskLevel = RiskLevel.LOW
    )
    public BookResponse create(BookCreateRequest request) {
        if (bookRepository.existsByIsbnIgnoreCase(request.isbn())) {
            throw new ConflictException("ISBN must be unique.");
        }
        Book book = bookMapper.toEntity(request);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    @Auditable(
            action = UPDATE,
            targetType = "Book",
            targetId = "#result.id()",
            riskLevel = RiskLevel.MEDIUM
    )
    public BookResponse update(Long id, BookUpdateRequest request) {
        if (bookRepository.existsByIsbnIgnoreCaseAndIdNot(request.isbn(), id)) {
            throw new ConflictException("ISBN must be unique.");
        }
        Book book = loadBook(id);
        bookMapper.toEntity(request, book);
        return bookMapper.toResponse(bookRepository.save(book));
    }

    @Transactional
    @Auditable(
            action = DELETE,
            targetType = "BOOK",
            targetId = "#id",
            riskLevel = RiskLevel.HIGH
    )
    public void delete(Long id) {
        bookRepository.delete(loadBook(id));
    }

    @Transactional(readOnly = true)
    public void runNPlusOneDiagnosis(int page, int size) {
        Page<Book> rawPage = bookRepository.findAll(BookSpecifications.fromFilter(new BookFilterRequest()),
                createPageable(page, size, "title:asc"));
        rawPage.getContent().forEach(bookMapper::mapAuthors);
    }

    private Pageable createPageable(int page, int size, String sort) {
        if (page < 0) {
            throw new BadRequestException("Page index must be zero or greater.");
        }
        if (size < 1) {
            throw new BadRequestException("Page size must be at least 1.");
        }
        if (size > properties.pagination().maxPageSize()) {
            throw new BadRequestException("Page size must not exceed %d.".formatted(properties.pagination().maxPageSize()));
        }
        return PageRequest.of(page, size, SortParser.parse(sort));
    }

    private Book loadBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book with id %d was not found.".formatted(id)));
    }
}
