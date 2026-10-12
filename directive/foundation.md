<!--
SYNC IMPACT REPORT
Version Change: (none) → 1.0.0
Bump Rationale: Initial foundation creation for Finance Tracker project. No prior version exists.

Modified Principles:
- (none — initial creation)

Added Sections:
- Purpose: Defines the mission and scope of the Finance Tracker application
- Core Principles: Six principles derived from Spring Boot + Java 17 REST API best practices
- Governance: Amendment procedure, versioning policy, and compliance review schedule
- Dependent Artifacts: Placeholder registry for downstream documents
- Foundation Metadata: Version, dates, and foundation type
- Review History: Initial entry

Removed Sections:
- (none — initial creation)

Template Updates:
✅ Generator skills reviewed and aligned with foundation changes
✅ plan-generator skill - No changes required; foundation type is default
✅ spec-generator skill - No changes required; scope reflects REST API constraints
✅ tasks-generator skill - No changes required; task categories align with principles

Deferred Items:
- (none)

Generated: 2026-03-09T00:00:00Z
-->

# Finance Tracker — Project Foundation

## Purpose

Finance Tracker is a personal finance tracking application built on Java 17 and Spring Boot.
It provides a REST API backend that enables individuals to record income and expenses, define
and monitor budgets, and set measurable financial goals. The system prioritizes correctness of
financial data, developer-friendly structure, and a layered architecture that supports future
growth without requiring disruptive rewrites.

This document is the authoritative source of project principles, architectural decisions, coding
conventions, and governance procedures. All contributors MUST treat it as the primary reference
when resolving design questions.

---

## Technology Stack

| Concern | Choice | Version / Notes |
|---|---|---|
| Language | Java | 17 (LTS) |
| Framework | Spring Boot | Latest stable (3.x line) |
| Build Tool | Maven or Gradle | Project standard to be set at repo init |
| API Style | REST (JSON) | HTTP/1.1; JSON request and response bodies |
| Persistence | Spring Data JPA | Hibernate as the default ORM provider |
| Database | Relational (e.g., PostgreSQL) | H2 for tests; configurable via profiles |
| Validation | Jakarta Bean Validation | `jakarta.validation` constraints on DTOs |
| Testing | JUnit 5 + Mockito + Spring Boot Test | Unit and integration test layers |
| Documentation | SpringDoc OpenAPI (Swagger UI) | Auto-generated from controller annotations |
| Security | Spring Security | JWT-based stateless authentication |

---

## Architecture Decisions

### Layered Architecture

The application MUST follow a strict three-tier layered architecture:

- **Controller layer**: Handles HTTP concerns only — request parsing, response mapping,
  input validation delegation, and HTTP status codes.
- **Service layer**: Contains all business logic, transaction boundaries, and orchestration.
  Services MUST NOT depend on HTTP types (e.g., `HttpServletRequest`).
- **Repository layer**: Manages all persistence interactions via Spring Data JPA repositories.
  Raw SQL MUST only appear in `@Query` annotations when JPQL is insufficient.

Cross-layer dependencies MUST only flow downward (Controller → Service → Repository).
No layer SHALL import from a layer above it.

### DTO Pattern

Entities MUST NOT be exposed directly through API responses or accepted as request bodies.
All data crossing the API boundary MUST travel through Data Transfer Objects (DTOs).
Mapping between entities and DTOs SHOULD use a dedicated mapper class or MapStruct.

### RESTful Resource Design

API endpoints MUST adhere to REST conventions:

- Resources are nouns in plural form (e.g., `/expenses`, `/budgets`).
- HTTP verbs express intent: `GET` (read), `POST` (create), `PUT`/`PATCH` (update),
  `DELETE` (remove).
- Responses MUST use semantically correct HTTP status codes.
- Pagination MUST be supported for all collection endpoints using Spring Data's `Pageable`.

### Configuration Over Hardcoding

All environment-specific values (database URLs, credentials, JWT secrets, feature flags)
MUST be externalized via `application.properties` / `application.yml` and MUST NOT be
hardcoded in source files. Spring profiles (`dev`, `test`, `prod`) SHALL be used to
manage environment differences.

### Fail-Fast Validation

Input validation MUST occur at the earliest possible boundary. DTO fields MUST carry
Jakarta Bean Validation constraints. A global `@ControllerAdvice` exception handler
MUST translate validation failures and domain exceptions into structured JSON error
responses with an appropriate HTTP status code and a human-readable message.

---

## Core Principles

### Principle 1: Financial Data Integrity First

**Requirements**

- All monetary values MUST be stored and computed using `java.math.BigDecimal`. The use of
  `double` or `float` for any financial calculation is strictly prohibited.
- Database columns storing monetary amounts MUST declare explicit precision and scale
  (e.g., `precision = 19, scale = 4`).
- All write operations that touch financial state (create, update, delete of transactions,
  budgets, or goals) MUST execute within a transaction boundary (`@Transactional`).
- Arithmetic operations on monetary values SHOULD use `BigDecimal.HALF_UP` rounding mode.

**Rationale**

Floating-point types introduce representational errors that are unacceptable in financial
contexts. Explicit precision and transactional guarantees protect users from silent data
corruption.

**Compliance Verification**

- Code review checkers MUST flag any use of `double` or `float` in financial model classes.
- Integration tests MUST assert that stored values round-trip through the API without
  precision loss.

---

### Principle 2: Layered Separation of Concerns

**Requirements**

- Classes MUST reside in the layer package appropriate to their responsibility:
  `controller`, `service`, `repository`, `model` (entities), `dto`, `mapper`, `config`,
  `exception`.
- Service classes MUST carry `@Service` and MUST be the sole location for business rules.
- Controller classes MUST carry `@RestController` and MUST contain no business logic.
- Repository interfaces MUST extend `JpaRepository` or `CrudRepository` and MUST NOT
  contain business logic.

**Rationale**

Strict layering reduces cognitive load, enables independent testing of each layer, and
prevents the gradual accumulation of logic in the wrong place.

**Compliance Verification**

- Architecture fitness functions (e.g., ArchUnit tests) SHOULD assert layer dependency
  direction and package placement.
- Pull request review checklist MUST include a layer-assignment check.

---

### Principle 3: Test Coverage as a First-Class Requirement

**Requirements**

- All service-layer methods MUST have unit tests using JUnit 5 and Mockito.
- All controller endpoints MUST have integration tests using `@SpringBootTest` with
  `MockMvc` or `WebTestClient`.
- Repository layer MUST be tested with `@DataJpaTest` against an in-memory H2 database.
- Overall line coverage MUST NOT fall below 80%. Branch coverage SHOULD target 70%.
- Tests MUST be deterministic — no reliance on external services, wall-clock time without
  mocking, or mutable shared state.

**Rationale**

Finance applications handle sensitive user data; regressions carry tangible user impact.
A strong test suite is the primary safety net for refactoring and feature evolution.

**Compliance Verification**

- CI pipeline MUST run the full test suite and enforce the coverage threshold.
- Coverage reports MUST be published as a CI artifact on every pull request.

---

### Principle 4: Explicit, Consistent API Contracts

**Requirements**

- Every public endpoint MUST be annotated with SpringDoc/OpenAPI annotations
  (`@Operation`, `@ApiResponse`, `@Schema`) sufficient to generate an accurate Swagger UI.
- Breaking changes to existing API contracts (field removal, type change, path rename)
  MUST increment the API version (`/api/v1/` → `/api/v2/`).
- All list/collection endpoints MUST support pagination via Spring Data `Pageable`
  parameters (`page`, `size`, `sort`).
- Error responses MUST follow a consistent JSON schema:
  `{ "timestamp", "status", "error", "message", "path" }`.

**Rationale**

A predictable, self-documenting API reduces integration effort for future frontend clients
or third-party consumers and prevents ambiguity when debugging production issues.

**Compliance Verification**

- OpenAPI spec MUST be generated and validated as part of the build.
- A dedicated integration test MUST assert the error response schema for at least one
  validation failure and one 404 path per resource.

---

### Principle 5: Security by Default

**Requirements**

- All API endpoints MUST require authentication unless explicitly opted out with documented
  justification (e.g., `/api/v1/auth/login`, `/api/v1/auth/register`).
- Authentication MUST use stateless JWT tokens. Sessions and server-side state MUST NOT
  be used.
- Passwords MUST be stored as BCrypt hashes. Plaintext passwords MUST never appear in
  logs, responses, or database columns.
- Sensitive configuration values (secrets, API keys) MUST be injected via environment
  variables or a secrets manager and MUST NOT be committed to version control.
- CORS configuration MUST be explicitly defined; wildcard origins (`*`) MUST NOT be used
  in production profiles.

**Rationale**

Personal finance data is highly sensitive. A default-secure posture ensures that
newly added endpoints are protected unless a deliberate, documented decision is made
to relax constraints.

**Compliance Verification**

- A Spring Security integration test MUST assert that unauthenticated requests to
  protected endpoints return `401 Unauthorized`.
- Repository secrets scanning MUST be enabled in CI to block credential commits.

---

### Principle 6: Clean Code and Java 17 Idioms

**Requirements**

- Code MUST compile without warnings at the `-Xlint:all` level, or each suppressed warning
  MUST carry a `@SuppressWarnings` annotation with a comment explaining the justification.
- Java 17 language features MUST be preferred where they improve readability: records for
  DTOs and value types, sealed interfaces for domain discriminated unions, pattern matching
  for `instanceof`, and text blocks for multi-line strings.
- Method length SHOULD NOT exceed 30 lines. Classes SHOULD NOT exceed 300 lines.
  Violations require a refactoring note in the pull request.
- All public APIs (public methods on service and controller classes) MUST carry Javadoc
  describing purpose, parameters, and return value.
- `var` MAY be used for local variables where the type is unambiguous from the right-hand
  side; it MUST NOT be used when the inferred type is not obvious from reading the line.

**Rationale**

Consistent, idiomatic Java 17 code reduces onboarding time, surfaces intent clearly, and
avoids accumulation of legacy patterns that obscure business logic.

**Compliance Verification**

- Static analysis (e.g., Checkstyle, SpotBugs) MUST be integrated into the build.
- Javadoc completeness MUST be verified for public service and controller methods.

---

## Governance

### Amendment Procedure

1. Any contributor MAY propose an amendment by opening a pull request that modifies this
   document and updates the Sync Impact Report HTML comment at the top of the file.
2. Proposals MUST describe: (a) the change being made, (b) the rationale, and (c) the
   impact on dependent artifacts.
3. Amendments require approval from at least one project maintainer before merging.
4. MAJOR version changes (principle removals, governance restructuring) require documented
   consensus from all active maintainers.
5. Once merged, the maintainer MUST update all dependent artifacts identified in Phase 4
   of the Foundation Architect protocol within the same release cycle.

### Versioning Policy

This document follows semantic versioning:

- **MAJOR (X.0.0)**: Removal of a principle, fundamental redefinition of a principle's
  intent, or overhaul of the governance structure.
- **MINOR (x.Y.0)**: Addition of a new principle, addition of a major section, or
  material expansion of an existing principle's requirements.
- **PATCH (x.y.Z)**: Clarifications, wording improvements, typo corrections, reordering
  without semantic change, or updates to the technology stack table that do not alter
  principles.

### Compliance Reviews

- A lightweight compliance review SHOULD be conducted at the start of each development
  sprint to ensure active work aligns with current principles.
- A full compliance audit MUST be conducted before each minor or major release.
- Audit findings MUST be recorded in the Review History section of this document.
- Non-compliant code MUST be tracked as technical debt with a linked issue and a
  target resolution sprint.

---

## Dependent Artifacts

The following artifacts derive requirements or structure from this foundation. They MUST
be reviewed and updated whenever a MINOR or MAJOR version change occurs.

| Artifact | Path | Status |
|---|---|---|
| API specification | `directive/spec.md` | Pending creation |
| Development plan | `directive/plan.md` | Pending creation |
| Task breakdown | `directive/tasks.md` | Pending creation |
| README | `README.md` | Pending creation |
| CI/CD pipeline config | `.github/workflows/ci.yml` | Pending creation |
| Checkstyle configuration | `config/checkstyle.xml` | Pending creation |
| ArchUnit test suite | `src/test/java/.../ArchitectureTest.java` | Pending creation |

---

## Foundation Metadata

| Field | Value |
|---|---|
| **Project Name** | Finance Tracker |
| **Foundation Version** | 1.0.0 |
| **Foundation Type** | default |
| **Ratification Date** | 2026-03-09 |
| **Last Amended Date** | 2026-03-09 |
| **Primary Language** | Java 17 |
| **Primary Framework** | Spring Boot 3.x |
| **API Style** | REST / JSON |
| **Maintainers** | TODO(MAINTAINERS): Add maintainer names or team alias |

---

## Review History

| Version | Date | Author | Summary |
|---|---|---|---|
| 1.0.0 | 2026-03-09 | Foundation Architect | Initial foundation creation for Finance Tracker Spring Boot + Java 17 project |
