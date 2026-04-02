# E-Commerce Backend

REST API for an e-commerce platform built with Spring Boot 3.x and Java 17.

## Tech Stack

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

## Build

```bash
./mvnw clean package
```

## Run

```bash
./mvnw spring-boot:run
```

The server starts on `http://localhost:8080`

## Project Structure

```
src/main/java/com/carlosarroyoam/ecommerce/
├── admin/          # Admin module
├── category/      # Category module
├── core/          # Shared components (filters, exceptions, DTOs)
├── customer/      # Customer module
├── inventory/     # Inventory module
├── order/         # Order module
├── product/       # Product module
├── refund/        # Refund module
├── shipment/      # Shipment module
├── user/          # User/Auth module
└── ECommerceApplication.java
```

## API Documentation

See `docs/openapi/api-docs.yaml` for OpenAPI specification.

## Database Schema

Schema defined in `src/main/resources/schema.sql` with initial data in `src/main/resources/data.sql`.