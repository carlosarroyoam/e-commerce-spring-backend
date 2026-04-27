# E-Commerce Backend

REST API for an e-commerce platform built with Spring Boot 3.x and Java 17.

## Technology Stack

- **Framework:** Spring Boot 3.x
- **Language:** Java 17
- **Database:** MySQL 8.x
- **ORM:** Spring Data JPA
- **Build Tool:** Maven

## Dependencies

- spring-boot-starter-web
- spring-boot-starter-data-jpa
- mysql-connector-j
- lombok
- mapstruct

## Prerequisites

- Java 17+
- Maven 3.8+
- MySQL 8.0+

## Configuration

### RSA Keys

The application needs RSA keys in `src/main/resources/certs/`. To generate new keys:

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

The server starts on `http://localhost:8080`

## API Documentation

See `docs/openapi/api-docs.yaml` for OpenAPI specification.

## Database Schema

Schema defined in `src/main/resources/schema.sql` with initial data in `src/main/resources/data.sql`.
