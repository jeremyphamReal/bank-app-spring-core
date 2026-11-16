# Bank App Spring Core

A REST API for a small banking application built with Spring Boot. The project demonstrates JWT-based authentication, BCrypt password hashing, JPA persistence, account management, deposits, withdrawals, transfers, and paginated transaction history.

> **Portfolio note:** This is an educational backend project. It is not production-ready financial software.

## Features

- Stateless authentication with JWT
- Login through Spring Security's `AuthenticationManager`
- BCrypt password hashing
- Account profile lookup
- Password change
- Deposit and withdrawal workflows
- Account-to-account transfers
- Transaction history with pagination
- Pessimistic database locking for balance-changing operations
- Consistent response wrapper through `ApiResponse`
- Unit tests for core account-service behavior and login controller behavior

## Technology stack

- Java 17
- Spring Boot
- Spring Web
- Spring Security
- Spring Data JPA / Hibernate
- MySQL
- JSON Web Token (JJWT)
- Maven
- JUnit 5 and Mockito
- Lombok

## Architecture

The application follows a layered structure:

```text
controller  ->  service  ->  repository  ->  MySQL
                   |
                 models / DTOs
```

- **Controllers** expose HTTP endpoints and map authenticated users to the current account.
- **AccountService** contains authentication, password, balance, transfer, and transaction-history logic.
- **Repositories** use Spring Data JPA for persistence.
- **JWT filter** extracts and validates bearer tokens before protected requests reach controllers.
- **DTOs** prevent controllers from returning the persistence entities directly for the main API responses.

## API endpoints

The application runs on port `9000` by default.

| Method | Endpoint | Authentication | Description |
|---|---|---:|---|
| `POST` | `/api/v1/auth/login` | No | Authenticate and return a JWT |
| `GET` | `/api/v1/accounts/me` | Yes | Get the authenticated user's profile and balance |
| `PUT` | `/api/v1/accounts/change-password` | Yes | Change the authenticated user's password |
| `POST` | `/api/v1/transactions/deposit` | Yes* | Deposit money into the current account |
| `POST` | `/api/v1/transactions/withdraw` | Yes* | Withdraw money from the current account |
| `POST` | `/api/v1/transactions/transfer` | Yes* | Transfer money to another account |
| `GET` | `/api/v1/transactions/history` | Yes* | Get paginated transaction history |
| `POST` | `/api/v1/transfers/transfer` | Yes* | Alternative transfer endpoint |

`*` The intended behavior is authenticated access. The current security configuration permits `/api/v1/transactions/**`; this is documented as a security item to fix before production use.

### Login example

```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "demo",
  "password": "your-password"
}
```

Successful responses use the following shape:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "token": "<jwt-token>"
  },
  "timestamp": "2026-01-01T10:00:00"
}
```

Use the token on protected requests:

```http
Authorization: Bearer <jwt-token>
```

### Transaction examples

```json
// POST /api/v1/transactions/deposit
{ "amount": 200000 }
```

```json
// POST /api/v1/transactions/transfer
{
  "toUsername": "receiver",
  "amount": 300000
}
```

## Local setup

### Prerequisites

- JDK 17
- MySQL 8 or compatible MySQL server
- Maven, or the included Maven Wrapper

### Database

Create a database, then configure the connection using environment variables or a local, untracked properties file. Do not commit credentials or JWT secrets.

Example local configuration:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bank_app_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
server.port=9000
```

For a real deployment, replace `spring.jpa.hibernate.ddl-auto=update` with a migration-based workflow such as Flyway or Liquibase.

### Run

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The API is available at `http://localhost:9000`.

### Test

```bash
./mvnw test
```

The current test suite includes:

- `AuthControllerTest`: verifies the login request, authentication manager interaction, JWT generation, and successful response.
- `AccountServiceTest`: verifies deposits, withdrawals, insufficient-funds behavior, transfers, and transaction persistence.

## Project structure

```text
src/
├── main/
│   ├── java/.../
│   │   ├── config/          # Security, JWT, and application beans
│   │   ├── controller/      # REST endpoints
│   │   ├── models/          # Entities, enums, request/response DTOs
│   │   ├── repository/      # Spring Data repositories
│   │   └── service/         # Account and transaction business logic
│   └── resources/
│       └── application.properties
└── test/
    └── java/.../            # Unit and controller tests
```

## Important limitations and improvement roadmap

The following items should be addressed before presenting this as a production-grade banking backend:

1. **Fix authorization rules.** `POST /api/v1/transactions/**` and `GET /api/v1/accounts/**` are currently configured as permitted routes, which can bypass the intended authentication boundary. Restrict them to authenticated users and test the security rules with `MockMvc`.
2. **Remove secrets from configuration.** Database credentials, JWT secrets, SQL logging, and verbose Spring Security logging must not be hardcoded or enabled in production. Use environment variables or a secrets manager.
3. **Add registration API and validation.** `AccountService` contains registration logic, but there is no active registration controller and the current login/password DTOs have limited validation constraints.
4. **Validate monetary inputs consistently.** Reject null, zero, negative, and excessively precise amounts for deposits and withdrawals, not only transfers. Consider storing money as integer minor units or enforcing a strict scale.
5. **Prevent self-transfers explicitly.** The current service allows a transfer where sender and recipient are the same account. Return a clear client error instead.
6. **Use domain-specific exceptions.** Replace generic `RuntimeException` and ambiguous `UsernameNotFoundException` usage with exceptions mapped by a centralized `@RestControllerAdvice`.
7. **Improve transaction consistency.** Save both account updates and both transfer records atomically, and define a deterministic lock order to reduce deadlock risk under concurrent transfers.
8. **Add database constraints.** Enforce unique usernames, non-negative balances, required fields, and suitable indexes at the database level.
9. **Fix response and error contracts.** The `ApiResponse.success(data, message)` overload currently does not use the supplied message. Standardize response generics and validation/error payloads.
10. **Clean up the build and architecture.** Remove duplicate Maven dependencies, use constructor injection consistently, remove legacy/commented controller code, rename classes to standard Java conventions, and separate transfer responsibilities from the general account service.
11. **Expand test coverage.** Add tests for invalid input, missing users, login failures, expired/invalid JWTs, password changes, transaction history mapping, controller authorization, and concurrent balance updates.
12. **Use database migrations and observability.** Replace automatic schema updates with Flyway/Liquibase and add structured logging, audit identifiers, health checks, and metrics.

## Security disclaimer

This repository is intended for learning and portfolio demonstration. Do not use it to process real money or sensitive financial data without a security review, threat model, audit logging, secure secret management, proper authorization, database migrations, and comprehensive integration testing.
