# E-Commerce Backend

REST API for an e-commerce platform built with Spring Boot 3.5 and Java 17.

## Technology Stack

- **Framework:** Spring Boot 3.5
- **Language:** Java 17
- **Database:** MySQL 8
- **Persistence:** Spring Data JPA (Hibernate)
- **Security:** Spring Security + OAuth2 Resource Server (self-issued RS256 JWT)
- **Mapping:** MapStruct
- **Build Tool:** Maven (wrapper included — `./mvnw`)

## Dependencies

Runtime:

- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-oauth2-resource-server
- spring-boot-starter-validation
- spring-boot-starter-actuator
- lombok
- mapstruct

Test:

- spring-boot-starter-test
- spring-security-test
- wiremock-spring-boot

See [`pom.xml`](pom.xml) for exact versions.

## Prerequisites

- Java 17+
- MySQL 8.0+
- Docker — required to run the integration tests (`*IT`), which spin up MySQL via Testcontainers
- Maven 3.8+ (or use the bundled `./mvnw` / `./mvnw.cmd` wrapper)

## Database Setup

Hibernate runs with `spring.jpa.hibernate.ddl-auto=validate` and there is no Flyway/Liquibase,
so the schema is **never** created automatically. Apply it manually before the first run:

```bash
mysql -u root -p < src/main/resources/schema.sql
mysql -u root -p < src/main/resources/data.sql
```

`schema.sql` is the source of truth for table structure; `data.sql` holds seed data.
Defaults: database `spring-boot-e-commerce`, user `root`, password `toor` (see below to override).

## Configuration

### Environment variables

All have sensible defaults for local development:

| Variable | Default | Description |
| --- | --- | --- |
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_USERNAME` | `root` | MySQL user |
| `DB_PASSWORD` | `toor` | MySQL password |
| `JWT_ACCESS_TOKEN_TTL_MS` | `300000` (5 min) | Access token lifetime |
| `JWT_REFRESH_TOKEN_TTL_MS` | `86400000` (1 day) | Refresh token lifetime |
| `JWT_REFRESH_TOKEN_MAX_LIFETIME_MS` | `2592000000` (30 days) | Max refresh-token chain lifetime |
| `COOKIE_SECURE` | `true` | Mark auth cookies as `Secure` (set `false` for plain HTTP) |

### RSA Keys

RSA keys for JWT signing live in `src/main/resources/certs/` (`private.pem`, `public.pem`) and
are already committed. Regenerate them only if needed:

```bash
# Generate RSA private key (2048 bits)
openssl genrsa -out src/main/resources/certs/keypair.pem 2048

# Extract public key
openssl rsa -in src/main/resources/certs/keypair.pem -pubout -out src/main/resources/certs/public.pem

# Convert to PKCS#8 format
openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in src/main/resources/certs/keypair.pem -out src/main/resources/certs/private.pem
```

## Build

```bash
./mvnw clean package
```

## Run

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`. Health check: `http://localhost:8080/actuator/health`.

## Test

```bash
./mvnw test                   # unit tests
./mvnw verify                 # unit + integration tests (*IT, via failsafe)
./mvnw test -Dtest=ClassName  # a single test class
```

Integration tests require a running Docker daemon — Testcontainers starts a `mysql:8.0` container
and applies `schema.sql`/`data.sql` to it automatically. A local MySQL is not needed for tests.

## API Documentation

- OpenAPI 3.1 spec: `docs/openapi/api-docs.yaml` (hand-maintained)
- Postman collection: `docs/postman/postman_collection.json`

## License

This project is licensed under Apache 2.0. See [`LICENSE`](LICENSE).
