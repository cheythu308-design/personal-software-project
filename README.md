# Document Management System

An AI-assisted, learning-oriented Java 17 / Spring Boot 3.5 application scaffold. The code is being developed in small, reviewable milestones with tests and documentation.

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


## Document metadata API

The document API supports metadata CRUD and binary file upload/download. Metadata is stored in MySQL; binary files are stored outside the database through a filesystem storage component.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/documents` | Create document metadata |
| GET | `/api/documents` | List documents, newest first |
| GET | `/api/documents/{id}` | Retrieve one document |
| PUT | `/api/documents/{id}` | Replace document metadata |
| DELETE | `/api/documents/{id}` | Delete document metadata |

Example request:

```json
{
  "title": "Architecture Guide",
  "description": "Initial system architecture",
  "fileName": "architecture.pdf",
  "contentType": "application/pdf",
  "fileSizeBytes": 2048
}
```

The API validates required titles and field lengths, returns `201 Created` with a Location header after creation, `204 No Content` after deletion, and RFC 9457-style problem details for validation and not-found errors.

Database schema is managed by Flyway migrations under `src/main/resources/db/migration`. Tests use an H2 in-memory database and run the same migration before Hibernate validates the schema.


### File upload and download

- `POST /api/documents/upload` — multipart upload; required part: `file`; optional fields: `title` and `description`.
- `GET /api/documents/{id}/file` — download the original file as an attachment.
- `GET /api/documents/{id}` and `GET /api/documents` return `fileAvailable` and `downloadUrl`.
- `DELETE /api/documents/{id}` deletes both the metadata record and its stored file.
- Empty files are rejected. The default maximum upload size is 10 MiB.
- The original filename is retained as metadata; the physical file is saved with a generated UUID-based storage key to prevent path traversal and filename collisions.

Example upload with curl:

```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -F "file=@./sample.pdf" \
  -F "title=Sample PDF" \
  -F "description=Example document"
```

Download:

```bash
curl -L http://localhost:8080/api/documents/1/file -o downloaded-file.pdf
```

### File storage configuration

- `DMS_STORAGE_LOCATION` — local directory for binary files; defaults to `./data/documents`.
- `DMS_MAX_FILE_SIZE` — maximum file size in bytes; defaults to `10485760` (10 MiB).
- Multipart request limits are configured in `src/main/resources/application.yml`.
- Local binary data under `data/` is excluded from Git. Back up this directory separately from the MySQL database.

This milestone uses local filesystem storage for development. For multi-instance production deployments, use a shared object store such as Amazon S3 or an equivalent service and configure access controls, malware scanning and retention policies.

## Planned, not yet implemented

File version history, authentication, role-based access control, audit events, notifications, search, reporting and production deployment hardening remain future milestones. They are not claimed as complete in this scaffold.

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
