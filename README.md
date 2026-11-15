# Document Management System

An AI-assisted, learning-oriented Java 17 / Spring Boot application scaffold. The code is being developed in small, reviewable milestones with tests and documentation.

## Implemented in this foundation milestone

- Spring Boot application bootstrap
- Liveness endpoint: `GET /api/health`
- Maven build with Java 17
- MySQL runtime configuration via environment variables
- H2 test profile for repeatable automated tests
- JUnit 5 / Spring Boot test setup
- Docker Compose configuration for local MySQL
- GitHub Actions build-and-test workflow
- OpenAPI UI dependency

## Planned, not yet implemented

Document metadata CRUD, file upload/download, version history, authentication, role-based access control, audit events, notifications, search, reporting and production deployment hardening are future milestones. They are not claimed as complete in this scaffold.

## Requirements

- JDK 17+
- Maven 3.9+
- Docker Desktop (optional; for the included MySQL service)

## Start the database

```bash
docker compose up -d mysql
```

The default local credentials are development-only examples in `docker-compose.yml`. Change them for any shared environment.

## Run the application

```bash
mvn spring-boot:run
```

Health endpoint: `http://localhost:8080/api/health`

OpenAPI UI: `http://localhost:8080/swagger-ui/index.html`

## Run tests

```bash
mvn test
```

Tests use the H2 in-memory database profile and do not require the MySQL container.

## Configuration

See `.env.example` for variable names. Export real values in your local shell or secret manager; never commit actual passwords, tokens, API keys, private keys or personal data.

## Development milestones

1. Foundation, build pipeline and health endpoint.
2. Document metadata model, persistence and CRUD APIs.
3. File storage abstraction, upload/download and validation.
4. Authentication, authorization and user management.
5. Audit trail, notifications and search.
6. Integration tests, observability and deployment hardening.

Implement, test, review and document each feature as it is actually developed.

## Project integrity and licensing

This repository is an AI-assisted scaffold, not evidence of 30,000 lines of human-written code or 30 genuine historical commits. Do not submit it to Project Sirius as qualifying code unless its rules are independently met through genuine authorship, ownership, history and licensing rights. Generated code must be disclosed accurately wherever required.
