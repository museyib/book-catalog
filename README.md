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
