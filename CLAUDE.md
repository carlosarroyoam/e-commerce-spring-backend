# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

REST API for an e-commerce platform: Spring Boot 3.5 (parent `3.5.16`) / Java 17, MySQL 8, Spring Data JPA (Hibernate), Spring Security as an OAuth2 resource server (self-issued RS256 JWT), MapStruct 1.6, Lombok. Maven with wrapper (`./mvnw`, `mvnw.cmd` on Windows). Package-by-feature under `com.carlosarroyoam.ecommerce`.

The API is almost entirely **read-only**: every feature exposes `GET` list/detail endpoints; the only writes are `/auth/*` (login, refresh-token, logout, plus `forgot-password`/`reset-password` which are not implemented), `PATCH /orders/{orderId}/cancel` and `DELETE /users/{userId}`.

## Directory layout

```
database/            schema.sql (source of truth for tables) + data.sql (seed). Also a Maven
                     resource dir, so both are on the classpath (used by the test profile).
docs/openapi/        api-docs.yaml — hand-maintained OpenAPI 3.1 spec
docs/postman/        postman_collection.json
src/main/java/com/carlosarroyoam/ecommerce/
  ECommerceApplication.java
  auth/              login/refresh/logout, TokenService, RefreshTokenService, Staff/CustomerDetailsService,
                     principal/ (AuthPrincipal, PrincipalType STAFF|CUSTOMER, token + mapper)
  user/ customer/ product/ category/ order/ payment/ shipment/ refund/ inventory/
  core/              config, constant, exception, filter, pagination, property, security, specification, util
src/main/resources/  application.properties, messages.properties, logback-spring.xml, certs/ (gitignored)
src/test/java/...    user/, auth/, core/exception/, support/ (test infrastructure), ECommerceApplicationTest
src/test/resources/  application-test.properties, sql/test-data-reset.sql, responses/** (JSON fixtures)
compose.yml, Dockerfile, .env.example
```

`inventory/` currently only has `dto/` and `entity/` (no controller/service/repository).

## Setup

1. **RSA keys** for JWT signing at `src/main/resources/certs/` (`private.pem`, `public.pem`). The directory is **gitignored** — a fresh clone must generate them, or the app fails to start (`RsaKeyProps` binds `app.rsa.*` as `@NotNull`):
   ```bash
   openssl genrsa -out src/main/resources/certs/keypair.pem 2048
   openssl rsa -in src/main/resources/certs/keypair.pem -pubout -out src/main/resources/certs/public.pem
   openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in src/main/resources/certs/keypair.pem -out src/main/resources/certs/private.pem
   ```
2. **Database.** `spring.jpa.hibernate.ddl-auto=validate`, no Flyway/Liquibase, and the main profile doesn't set `spring.sql.init.mode`, so nothing creates tables. Apply manually before the first local run:
   ```bash
   mysql -u root -p < database/schema.sql
   mysql -u root -p < database/data.sql
   ```
   Defaults: `localhost:3306`, database `spring-boot-e-commerce`, `root`/`toor`. Overridable with `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`. Note: as an OS env var, `SPRING_DATASOURCE_URL` overrides the whole `spring.datasource.url` property through relaxed binding, so pass a **full JDBC URL** (as `compose.yml` does), not just `host:port`.

   Other env-overridable settings: `JWT_ACCESS_TOKEN_TTL_MS` (default 5 min), `JWT_REFRESH_TOKEN_TTL_MS` (1 day), `JWT_REFRESH_TOKEN_MAX_LIFETIME_MS` (30 days), `COOKIE_SECURE` (default `true`).

## Common commands

```bash
./mvnw spring-boot:run       # run locally on http://localhost:8080
./mvnw clean package         # build the jar (runs unit tests)
./mvnw test                  # Surefire: *Test classes only
./mvnw verify                # Surefire + Failsafe: also runs *IT classes
./mvnw test -Dtest=UserServiceTest              # single unit test class
./mvnw verify -Dit.test=UserControllerIT -Dtest=none -Dsurefire.failIfNoSpecifiedTests=false  # single IT

docker compose up --build    # MySQL + backend (copy .env.example to .env first)
docker compose down          # stop, keep the data volume
docker compose down -v       # stop and drop the volume (reseeds on next up)
```

No linter or formatter plugin is configured.

### Docker Compose

`compose.yml` runs `mysql:8.0.35` and the backend (built from the multi-stage `Dockerfile`: Maven build with `-DskipTests`, then `eclipse-temurin:17-jre-alpine` as non-root user, healthcheck on `/actuator/health`). It mounts `database/schema.sql` and `database/data.sql` into `/docker-entrypoint-initdb.d/`, so MySQL applies them only on first boot of an empty `ecommerce-spring-data` volume. The backend waits for MySQL to be healthy.

Caveat: `.dockerignore` excludes `*.pem`, so `src/main/resources/certs/` is **not** copied into the build context and the image is built without RSA keys.

## Javadoc

All Javadoc is written in **Spanish**. Rules: third-person present-tense first sentence, `@throws` only for domain exceptions thrown by `UserService`/`AuthService`, REST methods guarded by `@RolesAllowed` end their first sentence with "Requiere rol {@code X}.", no `@author`/`@since`.

## Tests

`maven-failsafe-plugin` is bound in `pom.xml`, so classes ending in `IT` run only in `verify`, not in `test`. Everything that extends `support/AbstractIntegrationTest` (including the `ECommerceApplicationTest` smoke test, which does run in `test`) needs a reachable **Docker** daemon — not a local MySQL.

- `AbstractIntegrationTest` starts a singleton `mysql:8.0` Testcontainer (`withReuse(true)`, db `spring-boot-e-commerce`) wired via `@ServiceConnection`, activates `@ActiveProfiles("test")` and imports `JwtTestTokenFactory`.
- The `test` profile (`src/test/resources/application-test.properties`) sets `spring.sql.init.mode=always` and applies `schema.sql` → `sql/test-data-reset.sql` (TRUNCATEs the seeded tables so a reused container doesn't hit duplicate keys) → `data.sql`. `ddl-auto` stays `validate` on purpose so tests catch `schema.sql` drifting from the entities. It also sets `app.cookie.secure=false` and very short JWT TTLs (2 s access / 5 s refresh / 15 s max lifetime).

Existing tests: `user/` (`UserServiceTest`, `UserControllerTest`, `UserControllerIT`), `auth/` (`AuthServiceIT`, `RefreshTokenServiceIT`), `core/exception/GlobalExceptionHandlerTest`, and `ECommerceApplicationTest`.

### Test layers

- **`XServiceTest`** — pure Mockito unit tests with mocked repositories.
- **`XControllerTest`** — `MockMvc.standaloneSetup` slice: real `GlobalExceptionHandler` (built with `support/testutils/TestMessageSources.create()`), snake_case `ObjectMapper` from `support/testutils/TestObjectMappers`, and `support/security/FixedAuthPrincipalArgumentResolver` injecting a fixed `AuthPrincipal` (no security chain).
- **`XControllerIT`** — `@SpringBootTest(RANDOM_PORT)` + `@AutoConfigureMockMvc` + `@Transactional` against the container with the real security chain; real RS256 JWTs come from `JwtTestTokenFactory` (`staffToken`, `customerToken`, `expiredToken`). Response bodies are compared with `JSONAssert` against fixtures under `src/test/resources/responses/**` via `support/testutils/JsonUtils`.
- **Service ITs** (`AuthServiceIT`, `RefreshTokenServiceIT`) — `@SpringBootTest` against the container; deliberately **not** `@Transactional` when the test must observe committed state, cleaning up in `@AfterEach` instead.

### Test conventions

- Method names follow `givenX_whenY_thenZ`, and **every** test has a human-readable English `@DisplayName` ("Given ..., when ..., then ...").
- `JwtTestTokenFactory` puts `roles` in the claim as a **flat** list without the `ROLE_` prefix; the real `JwtAuthenticationConverter` maps each entry to `ROLE_*`. A nested list produces 403s.
- `GlobalExceptionHandlerTest` fails if any `AppMessages` constant has no entry in `messages.properties`.

## Architecture

### Feature package shape

Each domain package follows the same shape:

- `XController` — thin REST endpoints (`produces = "application/json"`), delegates to the service. Authorization via `@PreAuthorize` (e.g. `hasRole('ADMIN')` on `/users`); `@EnableMethodSecurity` is on.
- `XService` — `@Service`, `@Transactional`, business logic, throws domain exceptions.
- `XRepository` — `JpaRepository`, plus `JpaSpecificationExecutor` where the list endpoint supports filtering.
- `dto/` — response DTOs, request DTOs (only in `auth`) and `*Specs` filter DTOs.
- `entity/` — JPA entities plus status enums. The static metamodel (`User_`, `Order_`, …) is generated by `hibernate-jpamodelgen` at compile time — never hand-write it.

Annotation processors (in `maven-compiler-plugin`): `hibernate-jpamodelgen`, `lombok`, `mapstruct-processor`, `lombok-mapstruct-binding`.

### DTO mapping: MapStruct mapper nested in the DTO

Response DTOs are Lombok classes (`@Getter @Setter @Builder`, etc.) that declare their own MapStruct `@Mapper` interface as a nested type, obtained via `Mappers.getMapper(...)` — **not** a Spring bean:

```java
public class UserResponse {
  ...
  @Mapper(nullValuePropertyMappingStrategy = ..., unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {...})
  public interface UserResponseMapper {
    UserResponseMapper INSTANCE = Mappers.getMapper(UserResponseMapper.class);
    UserResponse toDto(User entity);
    List<UserResponse> toDtos(List<User> entities);
  }
}
```

Services call `XResponseMapper.INSTANCE.toDto(entity)`. Follow this pattern for new DTOs.

### Filtering and pagination

List endpoints bind query params to a `*Specs` DTO via `@ModelAttribute` and build a `Specification<T>` with `core/specification/SpecificationBuilder` (`SpecificationBuilder.<T>builder()...build()`), whose null-safe combinators — `equalsIfPresent`, `likeIfPresent`, `betweenIfPresent`, `betweenDatesIfPresent`, `inIfPresent`, `isNullIfPresent` — take metamodel paths. Pages are wrapped by `PagedResponse.PagedResponseMapper.INSTANCE.toPagedResponse(page)` into `{ items, pagination }`.

### JSON naming

`spring.jackson.property-naming-strategy=SNAKE_CASE`: Java fields are camelCase, request/response bodies are **snake_case** on the wire. Query params bound via `@ModelAttribute` are not affected and stay **camelCase**. Keep this split.

### Error handling

- Services throw subclasses of the abstract `core/exception/ApplicationException` (which carries its `HttpStatus`): `ResourceNotFoundException` (404), `BusinessException` (422), `ValidationException` (422), `ConflictException` (409), `ResourceAlreadyExistsException` (409), `UnauthorizedException` (401), `ForbiddenException` (403), `InternalServerException` (500, optional `cause`).
- The message passed to an exception is a **key** of `src/main/resources/messages.properties`, referenced through a constant in `core/constant/AppMessages`. To add a user-facing error: add the key to `messages.properties` and a constant to `AppMessages`; don't inline texts.
- `GlobalExceptionHandler` (`@RestControllerAdvice`) is the single translation point to RFC 9457 `ProblemDetail` (`application/problem+json`), built by `ProblemDetailFactory`: `type`, `title`, `status`, `detail`, `instance` (request path), plus an `errors` map (field → message) on 422 validation failures. It resolves the message key through `MessageSource` (falling back to the literal text). Handled: `ApplicationException`, unreadable body, type mismatch, no handler/resource, method not supported, `AuthenticationException`, `AccessDeniedException`, `MethodArgumentNotValidException` (→ **422**), and a catch-all `Exception` (→ 500).
- There is no `ResponseStatusException` handler: the unimplemented `forgot-password`/`reset-password` in `AuthService` throw it with 501, but it falls to the catch-all and surfaces as **500**.
- `CustomAuthenticationEntryPoint` and `CustomAccessDeniedHandler` (`core/security`) bypass the advice and serialize the same `ProblemDetail` with the injected `ObjectMapper`.
- **Logging of exceptions** happens only in `ExceptionLogger` (`@Component`), called by every handler above: `METHOD /path -> detail`, 4xx at `WARN` without stack trace, 5xx at `ERROR` with stack trace. Services and controllers must not log exceptions themselves.

### Logging / MDC

`logback-spring.xml` reuses Spring Boot's console config and only fills `LOG_CORRELATION_PATTERN` with `[traceId=<requestId>, principal=<principalType>:<userId>]`; the block disappears entirely outside an HTTP request. Only these filters (`core/filter`) write MDC keys, always clearing them in `finally`:

- `CorrelationIdFilter` — `@Component`, `Ordered.HIGHEST_PRECEDENCE`. MDC `requestId` from the `X-Request-Id` header or a new UUID; echoes it in the response header.
- `MdcUserContextFilter` — not a bean; added in `WebSecurityConfig` after `BearerTokenAuthenticationFilter`. Sets `userId`/`principalType` only when authenticated (otherwise `anonymous:anonymous`).
- `RequestLoggingFilter` — `@Component`; one `INFO` line per request: `METHOD URI status - N ms`.

Never put MDC keys from services/controllers.

### Auth model

- Two independent principal types: **STAFF** (`users` table, `user` package) and **CUSTOMER** (`customers` table, `customer` package), each with its own `UserDetailsService` (`StaffDetailsService`, `CustomerDetailsService`) and `AuthenticationProvider`, combined into one `AuthenticationManager`.
- JWTs are RS256 signed/verified with the local RSA key pair via Nimbus (`core/config/JwtConfig`), consumed as an OAuth2 resource server (no external IdP). Roles travel in a `roles` claim and map to `ROLE_*`. Controllers that need the caller get an `AuthPrincipal` argument (e.g. `UserController`).
- Stateless sessions. Refresh tokens are hashed (dedicated `refreshTokenPasswordEncoder` bean), bound to `(principal_id, principal_type, device_id)` in `refresh_tokens`, sent via cookie (`RefreshTokenCookie`, `CookieUtils`, `app.cookie.secure`).
- CSRF: `CookieCsrfTokenRepository` + `CsrfCookieFilter` (double-submit `X-XSRF-TOKEN`), ignored only for `/auth/login`.
- `/auth/**` and `/actuator/**` are `permitAll`; everything else requires authentication.
- CORS from `app.cors.*` (`CorsProps`): origin `http://localhost:4200`, methods `GET,POST,PUT,PATCH,DELETE,HEAD,OPTIONS` — add any new HTTP verb there or browser preflights fail.

### Configuration properties

`@ConfigurationProperties` classes live in `core/property`: `RsaKeyProps` (`app.rsa`), `JwtProps` (`app.jwt`), `CookieProps` (`app.cookie`), `CorsProps` (`app.cors`). Other notable settings: `spring.web.resources.add-mappings=false`, fixed locale `en_US`.

### Database

`database/schema.sql` (MySQL, InnoDB/utf8mb4) is the source of truth. Status-like columns are `VARCHAR` with `CHECK (... IN (...))` instead of lookup tables (e.g. `orders.status`, `payments.status`, `payments.method`), mirrored by Java enums in `entity/` (`OrderStatus`, `PaymentStatus`, `PaymentMethod`, `UserStatus`, `CustomerStatus`, …). When adding or renaming a value, keep `schema.sql`, the enum and `api-docs.yaml` in sync. `data.sql` holds the seed data (tests depend on it).

### API documentation

`docs/openapi/api-docs.yaml` is a hand-written OpenAPI 3.1 spec, not generated. Update it whenever a controller, DTO or `CHECK` constraint changes — nothing enforces it. A Postman collection lives in `docs/postman/postman_collection.json`.

## Code conventions

- **Javadoc in Spanish** is required (the `auth` package is the style reference): class-level Javadoc (1–3 sentences, `{@link}` to related types) on every public class; Javadoc on public **and private** methods of controllers, services and everything in `core/*`, with `@param`/`@return` where meaningful.
- Repositories: class-level Javadoc naming the entity, and Javadoc only on custom query methods.
- Entities: class-level Javadoc only, no per-field comments unless semantics are non-obvious.
- DTOs (request/response/`*Specs`): no Javadoc. Nested MapStruct mappers: no method-level Javadoc.
- Inline comments inside method bodies only for a genuinely non-obvious WHY.
- Commit messages are short, in Spanish, imperative third person (e.g. "Agrega @Validated", "Corrige typo").
