# Architecture overview

## Current foundation

The application is a Spring Boot modular monolith. Packages are organized by capability so that boundaries can be maintained as the domain grows without introducing distributed-system complexity prematurely.

- `config`: cross-cutting configuration such as API documentation.
- `health`: runtime liveness endpoint.
- `DocumentManagementApplication`: application bootstrap.

## Planned domain boundaries

1. **Identity and access** — users, roles and document permissions.
2. **Document catalogue** — document metadata, ownership, status and versions.
3. **Content storage** — upload/download abstraction; binary content is kept outside relational metadata tables.
4. **Audit** — append-only records for security-sensitive actions.
5. **Notifications** — delivery of document lifecycle events.

These are design targets, not implemented services yet. The initial delivery remains one deployable application; extraction into microservices should only happen when operational and scaling needs justify it.

## Data and consistency

MySQL is the intended relational system of record. Document metadata and access rules belong in the database. Binary content should be handled through a storage interface. When asynchronous events are introduced, use an outbox pattern to avoid publishing events for transactions that did not commit.

## Security principles

- Keep credentials in environment variables or a secret manager.
- Validate and constrain uploaded file type and size.
- Enforce authorization on every document operation, not only in the UI.
- Avoid logging file contents, tokens or personal data.
- Record audit events for permission and document lifecycle changes.

## Verification

The test profile uses H2 so unit/context tests do not depend on a running MySQL instance. Integration tests against MySQL can be added as a later milestone.
