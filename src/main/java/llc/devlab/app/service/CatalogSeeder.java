package llc.devlab.app.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import llc.devlab.app.config.AppProperties;
import llc.devlab.app.model.entity.Author;
import llc.devlab.app.model.entity.Book;
import llc.devlab.app.model.entity.Category;
import llc.devlab.app.repository.AuthorRepository;
import llc.devlab.app.repository.BookRepository;
import llc.devlab.app.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CatalogSeeder implements ApplicationRunner {

    private static final List<String> CATEGORY_NAMES = List.of(
            "Architecture", "Business", "Computing", "Design", "Education", "Fiction",
            "History", "Leadership", "Marketing", "Product", "Science", "Travel"
    );

    private final AppProperties properties;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public void run(@NonNull ApplicationArguments args) {
        if (!properties.seed().enabled()) {
            return;
        }
        if (bookRepository.count() > 0 || authorRepository.count() > 0 || categoryRepository.count() > 0) {
            return;
        }

        List<Category> categories = createCategories();
        List<Author> authors = createAuthors();
        createBooks(categories, authors);
    }

    private List<Category> createCategories() {
        List<Category> categories = CATEGORY_NAMES.stream().map(name -> {
            Category category = new Category();
            category.setName(name);
            category.setDescription(name + " titles for the shared catalog.");
            return category;
        }).toList();
        return categoryRepository.saveAll(categories);
    }

    private List<Author> createAuthors() {
        List<Author> authors = new ArrayList<>();
        for (int index = 1; index <= 100; index++) {
            Author author = new Author();
            author.setName("Author %03d".formatted(index));
            author.setBiography("Deterministic biography for author %03d.".formatted(index));
            authors.add(author);
        }
        return authorRepository.saveAll(authors);
    }

    private void createBooks(List<Category> categories, List<Author> authors) {
        List<Book> batch = new ArrayList<>();
        for (int index = 1; index <= 1_000; index++) {
            Book book = new Book();
            book.setTitle("Catalog Book %04d".formatted(index));
            book.setIsbn("ISBN-%04d-%04d".formatted(index / 100, index));
            book.setDescription("Deterministic seeded description for book %04d.".formatted(index));
            book.setPublicationYear(1980 + (index % 45));
            book.setRating(BigDecimal.valueOf(1.0 + ((index % 41) / 10.0)).setScale(2, java.math.RoundingMode.HALF_UP));
            book.setCategory(categories.get(index % categories.size()));

            Set<Author> bookAuthors = new HashSet<>();
            bookAuthors.add(authors.get(index % authors.size()));
            bookAuthors.add(authors.get((index + 7) % authors.size()));
            if (index % 3 == 0) {
                bookAuthors.add(authors.get((index + 17) % authors.size()));
            }
            book.setAuthors(bookAuthors);
            batch.add(book);

            if (batch.size() == 200) {
                bookRepository.saveAll(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            bookRepository.saveAll(batch);
        }
    }
}
