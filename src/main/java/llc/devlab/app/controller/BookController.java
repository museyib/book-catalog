package llc.devlab.app.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import llc.devlab.app.model.dto.book.*;
import llc.devlab.app.model.dto.common.CursorResponse;
import llc.devlab.app.model.dto.common.PageResponse;
import llc.devlab.app.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    ResponseEntity<PageResponse<BookSummaryResponse>> list(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be zero or greater.") int page,
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size must be at least 1.") int size,
            @RequestParam(required = false) String sort,
            @Valid @ModelAttribute BookFilterRequest filter
    ) {
        return ResponseEntity.ok(bookService.list(page, size, sort, filter));
    }

    @GetMapping("/cursor")
    ResponseEntity<CursorResponse<BookSummaryResponse>> listWithCursor(
            @RequestParam(defaultValue = "20") @Min(value = 1, message = "size must be at least 1.") int size,
            @RequestParam(required = false) String sort,
            @Valid @ModelAttribute BookCursorFilterRequest filter
    ) {
        return ResponseEntity.ok(bookService.list(size, sort, filter));
    }

    @GetMapping("/{id}")
    ResponseEntity<BookResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.get(id));
    }

    @PostMapping
    ResponseEntity<BookResponse> create(@Valid @RequestBody BookCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }

    @PatchMapping("/{id}")
    ResponseEntity<BookResponse> update(@PathVariable Long id, @RequestBody BookUpdateRequest request) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
