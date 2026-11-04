package llc.devlab.app.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.math.BigDecimal;
import java.util.Set;
import llc.devlab.app.model.dto.book.BookCreateRequest;
import llc.devlab.app.model.entity.Account;
import llc.devlab.app.model.entity.Author;
import llc.devlab.app.model.entity.Book;
import llc.devlab.app.model.entity.Category;
import llc.devlab.app.model.enums.Role;
import llc.devlab.app.repository.AccountRepository;
import llc.devlab.app.repository.AuthorRepository;
import llc.devlab.app.repository.BookRepository;
import llc.devlab.app.repository.CategoryRepository;
import llc.devlab.app.repository.RefreshTokenRepository;
import llc.devlab.app.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected final ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected AccountRepository accountRepository;

    @Autowired
    protected AuthorRepository authorRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected BookRepository bookRepository;

    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.execute("delete from refresh_token");
        jdbcTemplate.execute("delete from book_author");
        jdbcTemplate.execute("delete from book");
        jdbcTemplate.execute("delete from author");
        jdbcTemplate.execute("delete from category");
        jdbcTemplate.execute("delete from account");
    }

    protected String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(exception);
        }
    }

    protected String bearerTokenFor(String email) {
        return "Bearer " + accessTokenFor(email);
    }

    protected String accessTokenFor(String email) {
        Account account = accountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for token generation."));
        return jwtService.generateAccessToken(account);
    }

    protected Account saveAccount(String email, String password) {
        Account account = new Account();
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(password));
        account.setRole(Role.USER);
        return accountRepository.saveAndFlush(account);
    }

    protected Category saveCategory(String name) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(name + " category");
        return categoryRepository.saveAndFlush(category);
    }

    protected Author saveAuthor(String name) {
        Author author = new Author();
        author.setName(name);
        author.setBiography(name + " biography");
        return authorRepository.saveAndFlush(author);
    }

    protected Book saveBook(
            String title,
            String isbn,
            int publicationYear,
            BigDecimal rating,
            Category category,
            Set<Author> authors
    ) {
        Book book = new Book();
        book.setTitle(title);
        book.setIsbn(isbn);
        book.setDescription(title + " description");
        book.setPublicationYear(publicationYear);
        book.setRating(rating);
        book.setCategory(category);
        book.setAuthors(authors);
        return bookRepository.saveAndFlush(book);
    }

    protected BookCreateRequest bookRequest(
            String title,
            String isbn,
            int publicationYear,
            BigDecimal rating,
            Long categoryId,
            Set<Long> authorIds
    ) {
        return new BookCreateRequest(
                title,
                isbn,
                title + " description",
                publicationYear,
                rating,
                categoryId,
                authorIds
        );
    }
}
