package llc.devlab.app.integration;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Set;
import llc.devlab.app.model.dto.auth.RegisterRequest;
import llc.devlab.app.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookControllerIntegrationTest extends IntegrationTestSupport {

    private String bearerToken;

    @BeforeEach
    void setUpCatalog() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(new RegisterRequest("cataloguer@example.com", "Password123"))))
                .andExpect(status().isCreated());
        bearerToken = bearerTokenFor("cataloguer@example.com");

        var fiction = saveCategory("Fiction");
        var science = saveCategory("Science");
        var history = saveCategory("History");

        var alice = saveAuthor("Alice Adams");
        var bob = saveAuthor("Bob Brown");
        var carol = saveAuthor("Carol Clark");

        saveBook("Alpha Patterns", "ISBN-0001", 2001, BigDecimal.valueOf(4.8), science, Set.of(alice, bob));
        saveBook("Beta Systems", "ISBN-0002", 1999, BigDecimal.valueOf(3.7), history, Set.of(bob));
        saveBook("Gamma Fiction", "ISBN-0003", 2010, BigDecimal.valueOf(4.2), fiction, Set.of(carol));
        saveBook("Delta Science", "ISBN-0004", 2020, BigDecimal.valueOf(4.9), science, Set.of(alice));
        saveBook("Epsilon History", "ISBN-0005", 2015, BigDecimal.valueOf(2.9), history, Set.of(alice, carol));
    }

    @Test
    void paginatedListReturnsFirstLastAndEmptyBoundaries() throws Exception {
        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "title:asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.pageNumber", equalTo(0)))
                .andExpect(jsonPath("$.pageSize", equalTo(2)))
                .andExpect(jsonPath("$.totalElements", equalTo(5)))
                .andExpect(jsonPath("$.totalPages", equalTo(3)))
                .andExpect(jsonPath("$.first", equalTo(true)))
                .andExpect(jsonPath("$.last", equalTo(false)));

        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("page", "2")
                        .param("size", "2")
                        .param("sort", "title:asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.first", equalTo(false)))
                .andExpect(jsonPath("$.last", equalTo(true)));

        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("title", "missing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", empty()))
                .andExpect(jsonPath("$.totalElements", equalTo(0)))
                .andExpect(jsonPath("$.first", equalTo(true)))
                .andExpect(jsonPath("$.last", equalTo(true)));
    }

    @Test
    void cursorListFiltersAfterCursorAndReturnsNextCursor() throws Exception {
        mockMvc.perform(get("/api/books/cursor")
                        .header("Authorization", bearerToken)
                        .param("size", "2")
                        .param("sort", "title:asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", contains("Alpha Patterns", "Beta Systems")))
                .andExpect(jsonPath("$.nextCursorId", equalTo(bookRepository.findAll().stream()
                        .filter(book -> book.getTitle().equals("Beta Systems"))
                        .findFirst().orElseThrow().getId().intValue())))
                .andExpect(jsonPath("$.hasNextPage", equalTo(true)));

        var cursor = bookRepository.findAll().stream()
                .filter(book -> book.getTitle().equals("Beta Systems"))
                .findFirst().orElseThrow().getId();

        mockMvc.perform(get("/api/books/cursor")
                        .header("Authorization", bearerToken)
                        .param("size", "2")
                        .param("sort", "title:asc")
                        .param("cursor", cursor.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", contains("Delta Science", "Epsilon History")))
                .andExpect(jsonPath("$.hasNextPage", equalTo(true)));
    }

    @Test
    void titleFilterWorks() throws Exception {
        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("title", "science"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", equalTo("Delta Science")));
    }

    @Test
    void authorFilterWorks() throws Exception {
        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("authorName", "alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));
    }

    @Test
    void categoryFilterWorks() throws Exception {
        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("category", "History"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void publicationYearRangeFilterWorks() throws Exception {
        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("publishedYearFrom", "2000")
                        .param("publishedYearTo", "2020"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)));
    }

    @Test
    void minimumRatingFilterWorks() throws Exception {
        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("minRating", "4.8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void combinedFiltersWorkTogether() throws Exception {
        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("authorName", "alice")
                        .param("category", "Science")
                        .param("publishedYearFrom", "2000")
                        .param("minRating", "4.8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].title", contains("Alpha Patterns", "Delta Science")));
    }

    @Test
    void multiFieldSortingUsesAllowListedFields() throws Exception {
        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("sort", "rating:desc,publicationYear:asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title", equalTo("Delta Science")))
                .andExpect(jsonPath("$.content[1].title", equalTo("Alpha Patterns")));
    }

    @Test
    void invalidInputsReturnBadRequestWithUsefulMessages() throws Exception {
        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("page", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("list.page: page must be zero or greater.")));

        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("list.size: size must be at most 100.")));

        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("sort", "unknown:asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo(
                        "Unsupported sort field 'unknown'. Allowed fields: title, publicationYear, rating."
                )));

        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("sort", "title:up"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo(
                        "Unsupported sort direction 'up' for field 'title'. Use asc or desc."
                )));

        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("publishedYearFrom", "2025")
                        .param("publishedYearTo", "2020"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", equalTo("Validation failed.")));

        mockMvc.perform(get("/api/books")
                        .header("Authorization", bearerToken)
                        .param("minRating", "9.0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field", equalTo("minRating")));
    }
}
