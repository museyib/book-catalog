# Book Catalog API

Spring Boot REST API for a shared book catalog with JWT authentication, Flyway-managed schema, pageable/filterable book search, and deterministic catalog seeding.

## Assumptions

- Package and Maven coordinates were taken from the existing skeleton: `llc.devlab.app` / `llc.devlab.app:book-catalog`.
- The project uses the catalog workload from the skill: `Book`, `Author`, and `Category`.
- Catalog data is shared across authenticated users. No per-user ownership column was added.
- Book list sorting uses `sort=field:direction,field:direction`. Allowed fields are `title`, `publicationYear`, and `rating`.

## Stack

- Spring Boot with MVC, Security, Validation, and Spring Data JPA
- PostgreSQL for runtime
- Flyway for schema migrations
- H2 for tests
- JWT access tokens plus refresh-token rotation/revocation
- Springdoc OpenAPI at `/swagger-ui.html`

## Domain Model

- `Book`
  - `title` required, max 200
  - `isbn` required, unique, max 20
  - `description` optional, max 4000
  - `publicationYear` required, range `1450..2100`
  - `rating` required, range `0.0..5.0`
  - belongs to one `Category`
  - has many `Author`s
- `Author`
  - `name` required, max 200
  - `biography` optional, max 4000
- `Category`
  - `name` required, unique, max 120
  - `description` optional, max 1000

`Book.category` stays lazy and `Book.authors` owns the `book_author` join table. Delete cascade was intentionally not enabled from books to shared authors/categories.

## Configuration

Environment-backed properties live in [application.yaml](/home/museyib/IdeaProjects/book-catalog/src/main/resources/application.yaml).

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_ISSUER`
- `JWT_ACCESS_TOKEN_EXPIRATION_SECONDS`
- `JWT_REFRESH_TOKEN_EXPIRATION_SECONDS`
- `APP_MAX_PAGE_SIZE`
- `APP_SEED_ENABLED`
- `APP_SQL_LOG_LEVEL`
- `APP_SQL_BIND_LOG_LEVEL`

Local defaults target PostgreSQL at `jdbc:postgresql://localhost:5432/book_catalog` with username/password `book_catalog`.

## Seed Data

`CatalogSeeder` inserts deterministic shared catalog data on startup when `APP_SEED_ENABLED=true` and the catalog tables are empty:

- 12 categories
- 100 authors
- 1,000 books

The seeder is idempotent in practice because it exits once any catalog data already exists.

## API Summary

Public routes:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /swagger-ui.html`
- `GET /v3/api-docs`

Protected routes:

- `GET|POST|PUT|DELETE /api/books`
- `GET|POST|PUT|DELETE /api/authors`
- `GET|POST|PUT|DELETE /api/categories`

## Audit Trail

The Audit Trail records selected successful service operations and lets administrators review those events. Its implementation lives in the `audit-trail` module, while the main application supplies the current actor and client IP providers and marks service methods for auditing.

### Event workflow

1. A service method annotated with `@Auditable` completes successfully.
2. `AuditAspect` runs after the method returns. It builds an event from the current actor, the annotation's action and target type, the client IP, and the configured target ID.
3. The target ID is a Spring Expression Language (SpEL) expression evaluated against the method parameters and return value. For example, `#id` reads an argument and `#result.id()` reads a returned record's ID. An empty expression leaves the target ID unset.
4. `AuditService` saves the event to `audit_event`. Flyway creates this table from migration `V2__create_audit_table.sql`.
5. An administrator can query the saved events through `GET /audit`.

Because the aspect uses `@AfterReturning`, failed operations do not create events. Current annotations cover book and category reads and writes, plus account registration, login, and logout. The action values are `CREATE`, `READ`, `UPDATE`, `DELETE`, `REGISTER`, `LOGIN`, and `LOGOUT`.

### Query audit events

`GET /audit` requires an authenticated user with the `ADMIN` role. All query parameters are optional and combine as AND filters:

| Parameter | Meaning |
| --- | --- |
| `actorId` | Exact actor ID, case-insensitive |
| `action` | Exact action enum value, such as `CREATE` or `LOGIN` |
| `targetType` | Exact target type, case-insensitive |
| `targetId` | Exact target ID, case-insensitive |
| `dateFrom` | Inclusive lower bound for event time, ISO-8601 instant |
| `dateTo` | Inclusive upper bound for event time, ISO-8601 instant |

Example:

```http
GET /audit?action=UPDATE&targetType=Book&dateFrom=2026-01-01T00:00:00Z&dateTo=2026-01-31T23:59:59Z
Authorization: Bearer <admin-access-token>
```

The response is a JSON array of events with `id`, `occurredAt`, `actorId`, `action`, `targetType`, `targetId`, and `ipAddress`. The endpoint currently returns the full matching result set without pagination.

### Adding audit coverage

Annotate the service method that represents the operation, setting its action and a stable target type. Use a SpEL `targetId` when an ID can be read from the method arguments or successful return value. The actor and IP are resolved automatically by the application's provider implementations. Events are currently emitted only for successful returns, so methods that throw exceptions are not recorded.

### Book List

`GET /api/books`

Query parameters:

- `page` default `0`, must be `>= 0`
- `size` default `20`, must be `1..APP_MAX_PAGE_SIZE`
- `sort` optional, format `field:direction,field:direction`
- `title` optional, contains match
- `authorName` optional, contains match
- `category` optional, exact category-name match, case-insensitive
- `publishedYearFrom` optional
- `publishedYearTo` optional
- `minRating` optional

Pagination response fields:

- `content`
- `pageNumber`
- `pageSize`
- `totalElements`
- `totalPages`
- `first`
- `last`

## Verification

Integration coverage was added for:

- auth register/login/refresh/logout flow
- pagination first/last/empty boundaries
- each book filter independently
- filter combinations
- multi-field sorting
- invalid page, size, sort field, sort direction, and filter input
- N+1 diagnosis comparing unoptimized vs optimized fetch plans

The intended verification command is:

```bash
./mvnw test
```

This session could not execute the Maven build because the environment does not currently expose a runnable `java`/`javac` toolchain to the workspace, so build/test status is still pending.

## N+1 Diagnosis

The diagnosis harness is in [NPlusOneDiagnosisIntegrationTest.java](/home/museyib/IdeaProjects/book-catalog/src/test/java/llc/devlab/app/integration/NPlusOneDiagnosisIntegrationTest.java). It:

- runs the pageable list query in an intentionally unoptimized path that touches lazy authors
- captures Hibernate prepared-statement counts
- runs the optimized two-step fetch plan used by the production list endpoint
- compares the before/after counts

Measured counts are intentionally not recorded here yet because the test could not be run in this environment without a usable JDK.
