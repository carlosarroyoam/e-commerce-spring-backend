# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

REST API for an e-commerce platform: Spring Boot 3.5 / Java 17, MySQL 8, Spring Data JPA, MapStruct, Lombok. Package-by-feature under `com.carlosarroyoam.ecommerce`.

## Setup

Before the app will start, two things must exist:

1. **Database.** `spring.jpa.hibernate.ddl-auto=validate` — Hibernate never creates/alters tables. There is no Flyway/Liquibase and no `spring.sql.init.mode`, so `schema.sql`/`data.sql` are **not** run automatically against MySQL (Spring Boot only auto-runs them for embedded DBs). Apply manually before first run:
   ```bash
   mysql -u root -p < src/main/resources/schema.sql
   mysql -u root -p < src/main/resources/data.sql
   ```
   DB connection defaults (overridable via `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD` env vars): `localhost:3306`, db `spring-boot-e-commerce`, user `root`/`toor`.

2. **RSA keys** for JWT signing, expected at `src/main/resources/certs/` (`private.pem`, `public.pem`) — already committed, but if regenerating:
   ```bash
   openssl genrsa -out src/main/resources/certs/keypair.pem 2048
   openssl rsa -in src/main/resources/certs/keypair.pem -pubout -out src/main/resources/certs/public.pem
   openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in src/main/resources/certs/keypair.pem -out src/main/resources/certs/private.pem
   ```

## Common commands

```bash
./mvnw spring-boot:run       # run locally, http://localhost:8080
./mvnw clean package         # build jar
./mvnw test                  # run tests
./mvnw test -Dtest=ClassName # run a single test class
```

Test note: the only test present (`ECommerceApplicationTest`) is a full `@SpringBootTest` context load with no test profile/embedded DB — `mvn test` needs a real, reachable MySQL with `schema.sql`/`data.sql` already applied, same as running the app.

There is no linter/formatter plugin configured in `pom.xml`.

## Architecture

### Package-by-feature layout

Each business domain is a top-level package (`auth`, `user`, `customer`, `product`, `category`, `inventory`, `order`, `payment`, `shipment`, `refund`) with the same internal shape:

- `XController` — REST endpoints (thin; delegates to service)
- `XService` — `@Service`, transactional, business logic, throws domain exceptions (`ApplicationException` subclasses, see Error handling)
- `XRepository` — `JpaRepository` + `JpaSpecificationExecutor` when the entity supports query-param filtering
- `dto/` — request/response DTOs and `*Specs` filter DTOs
- `entity/` — JPA entities. Static metamodel classes (`User_`, `Role_`, etc.) are generated at compile time by `hibernate-jpamodelgen` (annotation processor in `pom.xml`) and used for type-safe Criteria/Specification paths — don't hand-write them.

Cross-cutting code lives in `core/`: `config` (security, JWT), `constant` (`AppMessages` — centralized exception message strings, reused across services), `dto` (`PagedResponse`/`PaginationResponse`), `exception` (`GlobalExceptionHandler`, response factory), `filter`, `property` (`@ConfigurationProperties` classes), `security`, `specification` (`SpecificationBuilder`), `util`.

### DTO mapping: MapStruct nested in the DTO, not Spring beans

Response DTOs are plain Lombok (`@Getter @Setter @Builder`) classes that declare their own MapStruct `@Mapper` interface as a nested type, resolved via `Mappers.getMapper(...)` — **not** a Spring-managed bean:

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
Services call `XResponseMapper.INSTANCE.toDto(entity)` directly. Follow this pattern for new response DTOs rather than introducing standalone/Spring-injected mappers.

### Filtering and pagination

List endpoints bind query params to a `*Specs` DTO via `@ModelAttribute`, then build a `Specification<T>` with the generic `SpecificationBuilder` (`core/specification/SpecificationBuilder.java`) using fluent, null-safe combinators (`likeIfPresent`, `equalsIfPresent`, `betweenDatesIfPresent`, `inIfPresent`, `isNullIfPresent`) against static metamodel paths. Results are wrapped with `PagedResponse.PagedResponseMapper.INSTANCE.toPagedResponse(page)` into `{ items, pagination }`.

### Error handling

Services signal failures with a domain exception from `core/exception`: the abstract `ApplicationException` (carries its own `HttpStatus`) and its subclasses — `ResourceNotFoundException` (404), `BusinessException` (422), `ValidationException` (422), `ConflictException` (409), `ResourceAlreadyExistsException` (409), `UnauthorizedException` (401), `ForbiddenException` (403). Each takes a single message string (use an `AppMessages` constant). `BusinessException`/`ValidationException` are **422**, matching Bean Validation, not 400. `GlobalExceptionHandler` (`@RestControllerAdvice`) is the single place that turns all exception types into a Spring `ProblemDetail` (RFC 9457, `application/problem+json`) built by `ProblemDetailFactory` — a uniform body of `type`, `title`, `status`, `detail`, `instance` (the request path), plus an `errors` extension member (field → message map) on **422** validation failures. Its `handleApplicationException` reads `ex.getStatus()`/`ex.getMessage()`; `handleResponseStatus` is kept only as a safety net for the few remaining `ResponseStatusException` throws (the not-yet-implemented password-recovery endpoints in `AuthService`). `@ExceptionHandler` methods return the `ProblemDetail` directly (Spring infers the status from it). The two Spring Security handlers that bypass the advice (`CustomAuthenticationEntryPoint`, `CustomAccessDeniedHandler`) serialize the same `ProblemDetail` with the injected `ObjectMapper`. Bean validation failures (`MethodArgumentNotValidException`) map to **422**, not 400. Add new user-facing error strings to `AppMessages` rather than inlining them.

Exception **logging** is centralized in `ExceptionLogger` (`core/exception`, a `@Component`): it is the only place that logs an exception being translated to an HTTP error response, with a uniform format (`METHOD /path -> status detail`) and a fixed severity policy — 4xx client errors at `WARN` without a stack trace, 5xx at `ERROR` with the stack trace. It is called by every `@ExceptionHandler` in `GlobalExceptionHandler` and by both Spring Security handlers. Services and controllers must **not** log exceptions themselves — throw the domain exception and let `ExceptionLogger` record it once.

### Logging / MDC

Every log line emitted while a request is being processed carries an MDC block `[requestId principalType:userId]`, rendered right before the logger name. Two filters in `core/filter` populate it and clear their keys in a `finally`:

- `CorrelationIdFilter` — `@Component` at `Ordered.HIGHEST_PRECEDENCE` (runs outside the Spring Security chain, wrapping everything). Sets MDC key `requestId` from the incoming `X-Request-Id` header or a fresh UUID, and echoes it back in the `X-Request-Id` response header.
- `MdcUserContextFilter` — **not** a `@Component`; added to the security chain in `WebSecurityConfig` via `addFilterAfter(new MdcUserContextFilter(), BearerTokenAuthenticationFilter.class)`, so the JWT is already authenticated. Sets MDC keys `userId` (the `AuthPrincipal` id) and `principalType` (`STAFF`/`CUSTOMER`). Unauthenticated requests render as `anonymous:anonymous`; a 401 for a missing/invalid token is handled by `BearerTokenAuthenticationFilter` before this filter, so that one line has `requestId` but no user.

The log pattern lives in `src/main/resources/logback-spring.xml`, which reuses Spring Boot's default console config and only fills the correlation slot (`LOG_CORRELATION_PATTERN`). That slot is wrapped in `%replace(...)` so that when there is no `requestId` (startup, background threads — anything outside an HTTP request) the whole `[...]` block disappears from the line instead of showing empty. Nothing else logs the user or writes to the MDC — filters only.

### Auth model

Two independent principal types share the same JWT-based auth, not a single `users` table:
- **STAFF** (`user` package, `users` table) and **CUSTOMER** (`customer` package, `customers` table) each have their own `UserDetailsService` (`StaffDetailsService`/`CustomerDetailsService`) and `AuthenticationProvider` bean, combined into one `AuthenticationManager`.
- JWTs are RS256, signed/verified with the RSA key pair via Nimbus (`JwtConfig`), consumed as a Spring Security OAuth2 **resource server** (self-issued, self-validated — no external IdP). Roles are carried as a `roles` JWT claim and mapped to `ROLE_*` authorities; `@EnableMethodSecurity` is on.
- Sessions are stateless; refresh tokens are hashed and bound to `(principal_id, principal_type, device_id)` (see `refresh_tokens` table / `RefreshTokenService`).
- CSRF cookie protection (`CsrfCookieFilter`, double-submit `X-XSRF-TOKEN`) applies to non-`/auth/login` routes even though the API is stateless/token-based.

### JSON naming convention

`spring.jackson.property-naming-strategy=SNAKE_CASE` is set globally in `application.properties`. Java/DTO fields stay camelCase; **request and response bodies are serialized as snake_case on the wire**. Query parameters are bound directly to `*Specs`/other DTOs via `@ModelAttribute` and are unaffected by that Jackson setting, so they stay **camelCase**. This split is intentional — keep it when adding fields (see `docs/openapi/api-docs.yaml`, which documents the same convention).

### API documentation

`docs/openapi/api-docs.yaml` is a hand-maintained OpenAPI 3.1 spec (not generated from code) describing every path, schema, and enum. When changing a controller, DTO, or a `CHECK` constraint in `schema.sql`, update this file to match — there's no build-time check that keeps them in sync.

### Database

`src/main/resources/schema.sql` is the source of truth for table structure (MySQL, `InnoDB`/`utf8mb4`); status-like columns are plain `VARCHAR` with `CHECK (... IN (...))` constraints rather than lookup tables (e.g. `orders.status`, `payments.status`, `payments.method`) — keep enum values in `schema.sql`, JPA entities, and `api-docs.yaml` in sync when adding/renaming a status. `data.sql` holds seed data loaded on top of it.

### Documentation convention

Javadoc on classes and their methods (public and private) is an explicit, deliberate exception to the general "no comments unless the WHY is non-obvious" rule — it documents the WHAT/responsibility of the public surface, not implementation reasoning. Inline comments inside method bodies stay off-limits unless they explain a genuinely non-obvious WHY. The `auth` package is the style reference; write new Javadoc in Spanish, matching its tone (e.g. `CustomerDetailsService`, `AuthController`):

- **Class-level** (every public class/interface): 1-3 sentences describing its responsibility, using `{@link}` for related types.
- **Controllers/Services**: Javadoc on both public methods (endpoints / business operations) and private helper methods, with `@param`/`@return` where meaningful.
- **Repositories**: Javadoc only on custom query methods, not on methods inherited from `JpaRepository`/`JpaSpecificationExecutor`; the interface itself gets a class-level Javadoc naming the entity it manages.
- **DTOs**: not documented — skip Javadoc entirely on request/response/`*Specs` DTOs.
- **Entities**: class-level Javadoc only (what it represents); no per-field comments unless a field's semantics are non-obvious.
- **Nested MapStruct mappers** (`XResponseMapper`): no method-level Javadoc (`toDto`/`toDtos` are self-explanatory); a short class-level Javadoc is optional.
- **`core/*`** (config, security, filter, specification, util): class-level and method Javadoc (public and private), since this cross-cutting code is less self-evident than feature classes.
