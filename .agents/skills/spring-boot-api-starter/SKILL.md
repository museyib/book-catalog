---
name: spring-boot-api-starter
description: Create a new Java Spring Boot REST API with a reusable Maven, PostgreSQL, Flyway, JWT security, layered architecture, validation, and API documentation setup, using domain entities defined for each project.
metadata:
  short-description: Create a configurable Spring Boot REST API
---

# Spring Boot API Starter

Create a new REST API project with the common platform features below and domain features tailored to the user's definition. Domain classes vary from project to project; names such as Book and Author below are examples only. Use whatever domain classes the user defines. This skill is a reusable recipe and does not require the source Taskboard project or its Task and Label classes.

## Define the project

Before implementation, extract or ask for information that materially affects the model or API:

- Project name, Maven group/artifact, Java package, and desired Java/Spring Boot versions if prescribed.
- Domain entities, their fields and constraints, and relationships/cardinality.
- Which resources need CRUD, pagination, filtering, sorting, ownership, soft deletion, or other business rules.
- Whether authentication is needed. Default to the JWT account feature set below unless the user asks for a different security model.

Use reasonable conventional assumptions for incidental details and record them in the README. Avoid inventing consequential business rules. Example definition:

```text
Project: book-catalog; package: llc.devlab.app
Book: title (required), isbn (unique), publishedAt (optional)
Author: name (required), biography (optional)
Relationship: many-to-many; books can have multiple authors
Features: paginated book list, dynamic filters, multi-field sorting, DTOs, seeded data, and N+1 diagnosis
Authentication: JWT; catalog data is shared, not user-owned
```

## Common platform feature set

Unless the user requests a smaller or different setup, include:

- Java and Spring Boot with Maven Wrapper.
- Spring MVC REST controllers, Spring Data JPA, Bean Validation, and Lombok. Use MapStruct for entity/DTO mapping where it improves clarity.
- PostgreSQL runtime database and Flyway versioned migrations as the schema source of truth. Disable Hibernate schema creation/update; keep JPA mappings and SQL migrations aligned.
- JWT authentication: registration, login, short-lived access token, refresh token, refresh rotation/revocation, BCrypt password hashing, and protected API routes. Store token secrets in environment-based configuration, never source code.
- Centralized API error handling with a consistent error response; validation errors should identify invalid fields without leaking internals.
- Springdoc OpenAPI/Swagger UI. Permit documentation endpoints explicitly.
- Consistent pagination metadata for paginated list responses: page number, page size, total elements, total pages, and `first`/`last` flags. Enforce a maximum page size.
- JPA auditing for creation/update timestamps and, when useful, actor identity.
- H2 and Spring Boot test dependencies for automated tests; test configuration should not require a running PostgreSQL database.
- `application.yaml` with environment-variable overrides for database credentials and JWT settings, and a README with setup, configuration, routes, and domain behavior.

Select current compatible dependency versions from Spring Initializr or official dependency management when implementing. If adapting an existing project, preserve its established versions and conventions unless asked to change them. Do not copy sample app names, packages, endpoints, schema, or domain rules.

## Package layout

Use a clear layered structure under the chosen application package. Adapt names to the project, but keep responsibilities separated:

```text
<package>/
  controller/       REST endpoints
  service/          business rules and transactions
  repository/       Spring Data persistence
  mapper/           entity/DTO mapping
  model/entity/     JPA entities
  model/dto/        request and response models
  model/enums/      domain enums, when needed
  exception/        domain errors and global handler
  security/         auth controllers, DTOs, entities, services, repositories, config
```

Keep request/response DTOs separate from JPA entities. Never serialize password hashes, refresh-token internals, or persistence-only fields. Controllers should handle HTTP concerns; services should enforce business rules and ownership; repositories should encapsulate persistence queries.

## Domain implementation

For every requested entity:

1. Define fields, Java types, nullability, lengths, uniqueness, defaults, indexes, and API exposure from the project definition.
2. Define JPA relationships and foreign keys from the stated cardinality. Make the owning side, join table, cascade behavior, and deletion semantics deliberate. Do not cascade deletion to related records by default.
3. Create input/output DTOs with Bean Validation annotations. MapStruct may generate mappings, but verify update mappings do not overwrite fields that clients cannot change (such as IDs, audit fields, owner, or soft-delete flags).
4. Implement requested CRUD and list operations. For collection endpoints, validate page and size (page must be non-negative, size positive and no larger than a documented maximum). Return a stable response envelope containing summary DTO items plus page number, page size, total elements, total pages, and `first`/`last` booleans. Map these values from Spring Data `Page<T>` metadata or calculate them accurately in a deliberate wrapper. Allow-list sort fields and directions; support multiple sort fields when requested instead of passing arbitrary client strings into persistence sorting. Return HTTP 400 with a clear message for invalid pagination, filters, or sort input.
5. Apply authenticated-user ownership filters only to user-owned data. Implement soft deletion only when requested or needed by an explicit domain rule.
6. Add domain errors for missing resources, conflicts, invalid transitions, and access violations where applicable. Ensure error status and response are consistent.
7. Write the Flyway migration to match JPA: table/column names, constraints, enum representation, foreign keys, join tables, and indexes. Include account and token tables when JWT is enabled.

### Catalog workload example

The following Book/Author/Category model is an example workload for exercising the reusable patterns. It is not a required domain: substitute the entities and filters defined for the actual project. When this catalog workload is requested, implement all of these requirements:

- Model `Book`, `Author`, and `Category`. A book has many authors and belongs to one category. Make the owning side explicit (for example, `Book.authors` owns the many-to-many join table and `Book.category` owns the many-to-one foreign key); use `mappedBy` on inverse collections if present. Explain cascade and fetch choices in code comments or README. Avoid cascading remove from books to shared authors/categories. Keep `@ManyToOne` lazy to avoid loading category data unnecessarily; use an explicit fetch plan for list/detail responses rather than changing every association to eager.
- Seed at least 1,000 books and 100 authors through an idempotent migration or startup seeder. Prefer deterministic seed data. Avoid inserting duplicate records on restart, and ensure the seed respects foreign keys and realistic many-to-many associations. Document how the seed runs.
- Implement `GET /api/books?page=...&size=...&sort=...` with a documented maximum page size and the standard pagination envelope: content, page number, page size, total elements, total pages, `first`, and `last`.
- Support optional combinable filters: title contains, author name, category, publication year range, and minimum rating. Validate inputs (including range ordering and sensible rating/year bounds) and combine predicates using Spring Data JPA `Specification`, not a repository method for every filter combination. Use joins for related entities and prevent duplicate books where joins require `distinct`.
- Support multiple sort keys over title, publication year, and rating, each ascending or descending. Parse the requested sort expression and reject unknown fields/directions with a clear HTTP 400 response. Never use an unchecked client-supplied property path.
- Return a summary DTO from list endpoints and a full view DTO from detail endpoints. Never serialize JPA entities directly. Add explicit DTO projections/mapping and choose a fetch plan that supplies the required fields.
- Enable SQL logging for the diagnosis exercise. First run the list query while accessing authors in a way that demonstrates N+1; record the observed query count and explain why it grows with returned rows. Then fix it with a suitable `@EntityGraph` or fetch join and record the before/after counts using the same seed data and page size. Put the measured counts and method in the README. Avoid fetch-joining a collection in a way that breaks database pagination; if necessary, page IDs first and fetch the related data in a second bounded query. Do not invent query counts: run the measurement and report the actual results. Keep verbose SQL logging configurable and disabled or reduced for normal runs if appropriate.
- Add integration tests for first/last/empty pagination boundaries, each filter alone, combinations of filters, multi-field sorting, and invalid page, size, sort field/direction, and filter input. Assert HTTP 400 and a useful error body for invalid input. Seed enough test data to expose pagination/fetch behavior without making tests unnecessarily slow.

For example, a simple JPA entity with the owning many-to-many side might look like this (adapt fields and relationships to the actual domain):

```java
@Entity
@Table(name = "book", uniqueConstraints = @UniqueConstraint(name = "uk_book_isbn", columnNames = "isbn"))
@Getter @Setter
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String isbn;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "book_author",
        joinColumns = @JoinColumn(name = "book_id"),
        inverseJoinColumns = @JoinColumn(name = "author_id"))
    private Set<Author> authors = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
```

Use DTO validation at the API boundary, for example:

```java
public record BookCreateRequest(
    @NotBlank @Size(max = 200) String title,
    @NotBlank @Size(max = 20) String isbn,
    Set<Long> authorIds
) {}
```

A controller should delegate to a service and return DTOs rather than entities. For paginated endpoints, expose the metadata described above alongside the item list (a `Page<T>` response is acceptable only if it reliably exposes all of those fields in the API contract). For dynamic filters, build a composable JPA Specification; for example, each non-empty criterion adds a predicate and the final specification ANDs the predicates. Keep sort parsing separate and validate every requested field against the endpoint's allow-list:

```java
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
class BookController {
    private final BookService bookService;

    @GetMapping
    BookPageResponse<BookSummaryDto> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String sort,
            @Valid BookFilter filter) {
        return bookService.list(page, size, sort, filter);
    }

    @PostMapping
    ResponseEntity<BookDto> create(@Valid @RequestBody BookCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.create(request));
    }
}
```

The response wrapper should explicitly expose metadata and content, for example `content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `first`, and `last`. Use explicit response wrappers or `ResponseEntity` if the project requires them; status code and pagination format matter more than matching this snippet exactly. The DTO names in this illustrative controller represent a concise list summary and a separate detailed view, not entity serialization.

## Security and configuration

When JWT is enabled, keep registration/login/refresh/logout routes and API documentation public, and require authentication on domain routes by default. Configure authorization rules explicitly. Do not assume all authenticated users can access each other's data: enforce ownership in service/repository queries when the definition says records are private. If the data is shared, do not add artificial ownership columns.

Configure issuer/token expiration/secret and database URL/user/password through environment variables. Use safe local development defaults only where appropriate; document that production secrets must be supplied externally. Add CORS rules only when a client origin is specified or required by the deployment context.

## Completion workflow

- Inspect the destination before changing it; preserve unrelated existing files and conventions.
- Create the application and tests for the requested domain, common platform features, migrations, and documentation.
- Add tests for representative service behavior, validation, authentication/authorization, and persistence/API integration. For paginated/filterable list endpoints, cover pagination boundaries, each filter independently and in combination, sorting, invalid inputs, and maximum page size. Keep tests isolated from external services using H2 or suitable mocks. For the catalog workload example, also record measured N+1 query counts before and after the fetch-plan fix.
- Run the project's relevant test/build command when permitted, fix failures caused by the changes, and report what was run. Never state that verification passed if it was not run.
- Search generated files for placeholder/sample domain names and stale project identifiers; remove them from application config, schema, README, and routes.
- Do not add domain-specific extras such as CSV export, scheduled jobs, notifications, status workflows, or per-user ownership unless the new project's definition calls for them.
