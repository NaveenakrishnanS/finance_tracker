# Finance Tracker API — Feature Specification

**Feature Branch**: `feature/finance-tracker-api`
**Created**: 2026-03-09
**Status**: Ready for Review
**Input**: User description: "finance tracker using spring boot and java17"
**Foundation Version**: 1.0.0

---

## Overview

Finance Tracker is a personal finance management REST API built on Java 17 and Spring Boot 3.x.
It enables authenticated users to record income and expenses, categorize transactions, manage
budgets, and track progress toward financial goals. All monetary computations use
`java.math.BigDecimal` with `HALF_UP` rounding. The API is stateless, JWT-secured (2-hour
token expiry, re-login on expiry), and fully documented via SpringDoc OpenAPI. Single currency
only; multi-currency support is out of scope for the initial release. Database schema migrations
and default category seeding are managed by Liquibase.

---

## User Scenarios & Testing

### Primary User Story

As an authenticated user, I want to record and categorize my income and expenses, define
monthly budgets per category, and set savings goals so that I have a clear, accurate picture
of my personal financial health.

### Acceptance Scenarios

#### Authentication

1. **Given** a new visitor, **When** they POST to `/api/v1/auth/register` with a valid email
   and password, **Then** a new user account is created and a success response is returned.
2. **Given** a registered user, **When** they POST to `/api/v1/auth/login` with correct
   credentials, **Then** they receive a signed JWT access token valid for 2 hours.
3. **Given** an expired or invalid JWT, **When** a request is made to any protected endpoint,
   **Then** the API returns `401 Unauthorized` with a structured error body.

#### Transactions

4. **Given** an authenticated user, **When** they POST to `/api/v1/transactions` with a valid
   amount, date, category, type (INCOME or EXPENSE), and optional description, **Then** the
   transaction is persisted and the created resource is returned with `201 Created`.
5. **Given** an authenticated user, **When** they GET `/api/v1/transactions`, **Then** a
   paginated list of their own transactions is returned, sorted by date descending by default.
6. **Given** an authenticated user, **When** they GET `/api/v1/transactions/{id}` for a
   transaction that belongs to them, **Then** the transaction detail is returned.
7. **Given** an authenticated user, **When** they GET `/api/v1/transactions/{id}` for a
   transaction that belongs to another user, **Then** `403 Forbidden` is returned.
8. **Given** an authenticated user, **When** they PUT `/api/v1/transactions/{id}` with updated
   fields, **Then** the transaction is updated and the full updated resource is returned.
9. **Given** an authenticated user, **When** they DELETE `/api/v1/transactions/{id}`, **Then**
   the transaction is removed and `204 No Content` is returned.
10. **Given** a POST body with a missing or negative amount, **When** the request is submitted,
    **Then** the API returns `400 Bad Request` with a structured validation error.

#### Categories

11. **Given** an authenticated user, **When** they GET `/api/v1/categories`, **Then** the list
    of available categories is returned (system-default and any user-defined ones).
12. **Given** an authenticated user, **When** they POST `/api/v1/categories` with a unique name,
    **Then** a new user-defined category is created.
13. **Given** an attempt to create a category with a duplicate name for the same user,
    **When** the request is submitted, **Then** `409 Conflict` is returned.

#### Budgets

14. **Given** an authenticated user, **When** they POST `/api/v1/budgets` with a category, a
    monetary limit, and a period (month + year), **Then** the budget is created.
15. **Given** an authenticated user, **When** they GET `/api/v1/budgets`, **Then** all their
    budgets are returned, each including the current spend total vs. limit.
16. **Given** a user's spending in a category exceeds the budget limit for that period,
    **When** they retrieve that budget, **Then** the response includes an `overLimit: true`
    boolean indicator in the budget detail payload.

#### Goals

17. **Given** an authenticated user, **When** they POST `/api/v1/goals` with a name, a target
    amount, and a target date, **Then** the goal is created.
18. **Given** an authenticated user, **When** they POST `/api/v1/goals/{id}/contributions` with
    a positive amount, **Then** the contribution amount is added to the goal's
    `contributedAmount` and the goal's current progress is updated.
19. **Given** an authenticated user, **When** they GET `/api/v1/goals/{id}`, **Then** the
    response includes target amount, contributed amount, remaining amount, and percentage
    progress.
20. **Given** contributions that meet or exceed the target amount, **When** the goal is
    retrieved, **Then** its status is marked as ACHIEVED.

#### Reporting / Summary

21. **Given** an authenticated user, **When** they GET `/api/v1/reports/summary` with optional
    `startDate` and `endDate` query parameters, **Then** a summary of total income, total
    expenses, net balance, and a per-category breakdown of those totals for the period is
    returned.
22. **Given** an authenticated user, **When** they GET `/api/v1/reports/monthly-trend` with
    optional `startDate` and `endDate` query parameters, **Then** a month-by-month breakdown
    of income, expenses, and net balance is returned.

### Edge Cases

- Future-dated transactions are permitted; there is no upper or lower bound restriction on
  the transaction `date` field.
- Deleting a category that is referenced by existing transactions or budgets MUST be blocked;
  the API returns `409 Conflict` with a message identifying the constraint.
- Deletion of a system-default category MUST be rejected with `403 Forbidden`.
- Concurrent budget updates (two requests modifying the same budget at the same time) must
  not corrupt the spend totals. Optimistic locking via `@Version` should be considered.
- Pagination defaults (`page=0`, `size=20`) must apply when no pagination parameters are
  provided.
- Requests with a `size` value exceeding the maximum of 25 MUST be rejected with
  `400 Bad Request` and a clear error message.

---

## Requirements

### Functional Requirements

#### Authentication & User Management

- **FR-001**: The API MUST provide a public user registration endpoint that accepts email and
  password, validates their format, hashes the password with BCrypt, and persists a new user
  account.
- **FR-002**: The API MUST provide a public login endpoint that validates credentials and
  returns a signed JWT access token (valid for 2 hours) on success.
- **FR-003**: The API MUST reject requests to all non-public endpoints that do not carry a
  valid, non-expired JWT with `401 Unauthorized`.
- **FR-004**: The API MUST scope all data operations so that a user can only access and modify
  their own resources (transactions, budgets, goals, user-defined categories).
- **FR-005**: Re-login on JWT expiry is the accepted token-refresh strategy for the initial
  release. No dedicated refresh-token endpoint is required.
- **FR-006**: The API MUST provide user profile management endpoints supporting:
  change password (`PUT /api/v1/users/me/password`),
  update display name (`PUT /api/v1/users/me`), and
  delete account (`DELETE /api/v1/users/me`).

#### Transaction Management

- **FR-010**: The API MUST allow authenticated users to create a transaction with the following
  mandatory fields: `amount` (positive `BigDecimal`), `type` (`INCOME` or `EXPENSE`),
  `categoryId`, and `date`.
- **FR-011**: The API MUST allow authenticated users to supply an optional `description` field
  (free text) on a transaction.
- **FR-012**: The API MUST allow authenticated users to retrieve a paginated, filterable list
  of their transactions. Supported filters MUST include: `type`, `categoryId`, `startDate`,
  `endDate`.
- **FR-013**: The API MUST allow authenticated users to retrieve a single transaction by ID.
- **FR-014**: The API MUST allow authenticated users to update any field of an existing
  transaction they own.
- **FR-015**: The API MUST allow authenticated users to delete a transaction they own.
- **FR-016**: All transaction write operations MUST execute within a `@Transactional` boundary.
- **FR-017**: Future-dated transactions MUST be accepted without restriction.

#### Category Management

- **FR-020**: The system MUST provide a set of default categories seeded via Liquibase
  migration, available to all users. The initial default set is:
  Food, Transport, Utilities, Salary, Entertainment, Healthcare, Housing, Education, Other.
- **FR-021**: The API MUST allow authenticated users to create custom categories with a unique
  name scoped to their account.
- **FR-022**: The API MUST allow authenticated users to retrieve all categories available to
  them (system defaults plus their own custom categories).
- **FR-023**: The API MUST allow authenticated users to delete their own custom categories.
  Deletion of system-default categories MUST be rejected with `403 Forbidden`.
- **FR-024**: Deletion of a custom category that is referenced by one or more existing
  transactions or budgets MUST be blocked and MUST return `409 Conflict`.

#### Budget Management

- **FR-030**: The API MUST allow authenticated users to create a budget that associates a
  category with a monetary spending limit for a specific calendar month and year.
- **FR-031**: The API MUST prevent a user from creating more than one budget per
  category-per-month. Duplicate creation attempts MUST return `409 Conflict`.
- **FR-032**: The API MUST allow authenticated users to update the limit of an existing budget.
- **FR-033**: The API MUST allow authenticated users to delete an existing budget.
- **FR-034**: Budget list and detail responses MUST include a computed `spentAmount` field
  reflecting the sum of EXPENSE transactions in the corresponding category and period, and an
  `overLimit` boolean field set to `true` when `spentAmount` exceeds `limitAmount`.
- **FR-035**: The `spentAmount` MUST be computed at query time (or cached with documented
  invalidation rules) and MUST be consistent with persisted transaction data.

#### Goal Management

- **FR-040**: The API MUST allow authenticated users to create a savings goal with a `name`,
  `targetAmount` (positive `BigDecimal`), and `targetDate`.
- **FR-041**: The API MUST allow authenticated users to record a contribution toward a goal,
  specifying a positive `amount` and optional `note`. Each contribution is a simple additive
  operation that increments `contributedAmount` on the goal; no per-entry contribution history
  is exposed via the API.
- **FR-042**: Goal responses MUST include `targetAmount`, `contributedAmount`,
  `remainingAmount`, `progressPercentage`, and `status` (`IN_PROGRESS` or `ACHIEVED`).
- **FR-043**: The API MUST automatically set a goal's status to `ACHIEVED` when
  `contributedAmount >= targetAmount`.
- **FR-044**: The API MUST allow authenticated users to list, retrieve, update (name,
  targetAmount, targetDate), and delete their own goals.

#### Reporting

- **FR-050**: The API MUST provide a summary endpoint (`GET /api/v1/reports/summary`) returning
  total income, total expenses, net balance, and a per-category breakdown of income and
  expenses for a user-specified date range. If no dates are supplied, the current calendar
  month is used as the default range.
- **FR-051**: The API MUST provide a monthly trend endpoint (`GET /api/v1/reports/monthly-trend`)
  returning a month-year keyed breakdown of total income, total expenses, and net balance,
  enabling clients to render trend charts.

### Non-Functional Requirements

#### Security

- **NFR-001**: All API endpoints MUST require JWT authentication except `/api/v1/auth/register`
  and `/api/v1/auth/login`.
- **NFR-002**: Passwords MUST be stored exclusively as BCrypt hashes. Plaintext passwords
  MUST NOT appear in logs, responses, or database records.
- **NFR-003**: JWT secrets MUST be externalized via environment variables and MUST NOT be
  committed to version control.
- **NFR-004**: CORS MUST be explicitly configured; wildcard origins (`*`) MUST NOT be used in
  production profiles.
- **NFR-005**: JWT access tokens MUST expire after 2 hours. Re-login is required upon expiry.

#### Data Integrity

- **NFR-010**: All monetary values MUST be stored and computed using `java.math.BigDecimal`
  with a minimum precision of 19 and scale of 4.
- **NFR-011**: Use of `double` or `float` for any monetary calculation is strictly prohibited.
- **NFR-012**: Rounding MUST use `BigDecimal.HALF_UP` throughout all financial computations.
- **NFR-013**: All write operations affecting financial state MUST be wrapped in
  `@Transactional` boundaries.

#### Performance & Scalability

- **NFR-020**: The API MUST meet a p95 response time SLA of under 500ms for all endpoints
  under normal load.
- **NFR-021**: No specific concurrent-user capacity target is required for the initial release.
  Capacity planning will be revisited once production traffic baselines are established.
- **NFR-022**: All collection endpoints MUST support pagination via Spring Data `Pageable`
  (`page`, `size`, `sort` parameters). The default page size MUST be 20. The maximum page
  size MUST be 25; requests exceeding this limit MUST be rejected with `400 Bad Request`.

#### API Design

- **NFR-030**: All endpoints MUST be prefixed with `/api/v1/`.
- **NFR-031**: Breaking API changes MUST result in a version increment (e.g., `/api/v2/`).
- **NFR-032**: All public endpoints MUST be annotated with SpringDoc OpenAPI annotations
  (`@Operation`, `@ApiResponse`, `@Schema`) to produce an accurate Swagger UI.
- **NFR-033**: Error responses MUST conform to the schema:
  `{ "timestamp", "status", "error", "message", "path" }`.
- **NFR-034**: HTTP status codes MUST be semantically correct for all operations.

#### Testing

- **NFR-040**: Service-layer unit test coverage MUST use JUnit 5 and Mockito.
- **NFR-041**: Controller endpoint integration tests MUST use `@SpringBootTest` with `MockMvc`.
- **NFR-042**: Repository tests MUST use `@DataJpaTest` with H2 in-memory database.
- **NFR-043**: Overall line coverage MUST NOT fall below 80%; branch coverage SHOULD target 70%.
- **NFR-044**: A Spring Security integration test MUST assert that unauthenticated requests to
  protected endpoints return `401 Unauthorized`.

#### Code Quality

- **NFR-050**: Code MUST compile without warnings at `-Xlint:all` level, or each suppression
  MUST carry a documented `@SuppressWarnings` justification.
- **NFR-051**: Java 17 records MUST be used for DTO and value types.
- **NFR-052**: Method length SHOULD NOT exceed 30 lines; class length SHOULD NOT exceed
  300 lines.
- **NFR-053**: All public service and controller methods MUST carry Javadoc.
- **NFR-054**: Static analysis (Checkstyle, SpotBugs) MUST be integrated into the build.

---

## Key Entities & Data Models

### User

| Field | Type | Constraints |
|---|---|---|
| `id` | `Long` | Auto-generated PK |
| `email` | `String` | Unique, not null, valid email format |
| `passwordHash` | `String` | BCrypt hash, not null, never exposed via API |
| `displayName` | `String` | Optional |
| `createdAt` | `LocalDateTime` | Set on creation, not null |

### Category

| Field | Type | Constraints |
|---|---|---|
| `id` | `Long` | Auto-generated PK |
| `name` | `String` | Not null; unique per user (system categories are user-agnostic) |
| `isSystemDefault` | `boolean` | True for Liquibase-seeded defaults |
| `user` | `User` | Nullable (null = system default) |

### Transaction

| Field | Type | Constraints |
|---|---|---|
| `id` | `Long` | Auto-generated PK |
| `user` | `User` | Not null, FK |
| `amount` | `BigDecimal` | Positive, precision 19 scale 4, not null |
| `type` | `TransactionType` (enum) | `INCOME` or `EXPENSE`, not null |
| `category` | `Category` | Not null, FK |
| `date` | `LocalDate` | Not null; future dates are permitted |
| `description` | `String` | Optional, max 500 chars |
| `createdAt` | `LocalDateTime` | Set on creation |
| `updatedAt` | `LocalDateTime` | Updated on modification |

### Budget

| Field | Type | Constraints |
|---|---|---|
| `id` | `Long` | Auto-generated PK |
| `user` | `User` | Not null, FK |
| `category` | `Category` | Not null, FK |
| `limitAmount` | `BigDecimal` | Positive, precision 19 scale 4, not null |
| `month` | `int` | 1–12, not null |
| `year` | `int` | Not null |
| Unique constraint | — | (`user`, `category`, `month`, `year`) |

Budget response payload additionally includes:

| Computed Field | Type | Description |
|---|---|---|
| `spentAmount` | `BigDecimal` | Sum of EXPENSE transactions in the category for the budget period |
| `overLimit` | `boolean` | `true` when `spentAmount > limitAmount` |

### Goal

| Field | Type | Constraints |
|---|---|---|
| `id` | `Long` | Auto-generated PK |
| `user` | `User` | Not null, FK |
| `name` | `String` | Not null, max 200 chars |
| `targetAmount` | `BigDecimal` | Positive, precision 19 scale 4, not null |
| `contributedAmount` | `BigDecimal` | Running total, default 0, precision 19 scale 4 |
| `targetDate` | `LocalDate` | Not null |
| `status` | `GoalStatus` (enum) | `IN_PROGRESS` or `ACHIEVED` |
| `createdAt` | `LocalDateTime` | Set on creation |

Note: Contributions are simple additive operations that increment `contributedAmount` directly
on the `Goal` entity. No `GoalContribution` history table is persisted or exposed via the API.

---

## API Contract Summary

### Authentication (Public)

| Method | Path | Description | Request Body | Success |
|---|---|---|---|---|
| POST | `/api/v1/auth/register` | Register a new user | `{ email, password, displayName? }` | `201 Created` |
| POST | `/api/v1/auth/login` | Authenticate and obtain JWT | `{ email, password }` | `200 OK` with `{ token, expiresIn }` |

### User Profile (Protected)

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/api/v1/users/me` | Retrieve own profile | `200 OK` |
| PUT | `/api/v1/users/me` | Update display name | `200 OK` |
| PUT | `/api/v1/users/me/password` | Change password | `204 No Content` |
| DELETE | `/api/v1/users/me` | Delete account | `204 No Content` |

### Transactions (Protected)

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/api/v1/transactions` | List transactions (paginated, filterable; max page size 25) | `200 OK` (Page) |
| POST | `/api/v1/transactions` | Create a transaction | `201 Created` |
| GET | `/api/v1/transactions/{id}` | Get a single transaction | `200 OK` |
| PUT | `/api/v1/transactions/{id}` | Update a transaction | `200 OK` |
| DELETE | `/api/v1/transactions/{id}` | Delete a transaction | `204 No Content` |

### Categories (Protected)

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/api/v1/categories` | List all accessible categories | `200 OK` |
| POST | `/api/v1/categories` | Create a custom category | `201 Created` |
| DELETE | `/api/v1/categories/{id}` | Delete a custom category | `204 No Content` |

### Budgets (Protected)

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/api/v1/budgets` | List all budgets with spend totals and over-limit indicator | `200 OK` |
| POST | `/api/v1/budgets` | Create a budget | `201 Created` |
| GET | `/api/v1/budgets/{id}` | Get a single budget | `200 OK` |
| PUT | `/api/v1/budgets/{id}` | Update budget limit | `200 OK` |
| DELETE | `/api/v1/budgets/{id}` | Delete a budget | `204 No Content` |

### Goals (Protected)

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/api/v1/goals` | List all goals | `200 OK` |
| POST | `/api/v1/goals` | Create a goal | `201 Created` |
| GET | `/api/v1/goals/{id}` | Get a single goal | `200 OK` |
| PUT | `/api/v1/goals/{id}` | Update a goal | `200 OK` |
| DELETE | `/api/v1/goals/{id}` | Delete a goal | `204 No Content` |
| POST | `/api/v1/goals/{id}/contributions` | Add a contribution amount to a goal | `200 OK` |

### Reports (Protected)

| Method | Path | Description | Success |
|---|---|---|---|
| GET | `/api/v1/reports/summary` | Income/expense summary with per-category breakdown for a date range | `200 OK` |
| GET | `/api/v1/reports/monthly-trend` | Month-by-month income/expense/net breakdown | `200 OK` |

---

## Error Response Schema

All error responses MUST conform to this JSON structure:

```json
{
  "timestamp": "2026-03-09T14:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "amount must be greater than 0",
  "path": "/api/v1/transactions"
}
```

Validation errors with multiple field violations SHOULD include a `fieldErrors` array:

```json
{
  "timestamp": "2026-03-09T14:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/transactions",
  "fieldErrors": [
    { "field": "amount", "message": "must be greater than 0" },
    { "field": "date", "message": "must not be null" }
  ]
}
```

---

## Assumptions Made

1. This is a single-user-per-account model; no shared household or multi-user collaboration
   is required for the initial release.
2. The primary persistence store is a relational database (PostgreSQL for production, H2 for
   tests), managed via Spring Data JPA with Hibernate.
3. Database schema migrations and default category seeding are managed by Liquibase.
4. The API serves a single frontend client (e.g., a future web or mobile app); multi-tenant
   or partner API concerns are out of scope for the initial release.
5. All transactions use a single, fixed currency. Multi-currency support is explicitly out of
   scope for the initial release.
6. No file attachments (e.g., receipt images) are required for the initial release.
7. Soft-delete (logical deletion) is not required unless explicitly requested; hard deletes
   are used for all resources.
8. Goal contributions are simple additive increments to the goal's `contributedAmount` field;
   no per-entry history table is required for the initial release.

---

## Clarifications Received

The following questions were raised during initial specification drafting. All have been
answered and incorporated into the specification above.

| # | Question | Answer |
|---|---|---|
| 1 | Should the API proactively notify the user when a budget is exceeded, or is a simple over-limit indicator in the budget detail sufficient? | Simple over-limit indicator (`overLimit: boolean`) in the budget detail response — no proactive notification needed. |
| 2 | Should the summary report also break down totals by category? | Yes — include a per-category breakdown of income and expense totals in addition to aggregate totals. |
| 3 | Are future-dated transactions permitted? If not, what is the allowable date range? | Yes — future-dated transactions are permitted; no date range restriction applies. |
| 4 | Should deletion of a referenced category be blocked (`409 Conflict`) or soft-deleted/archived? | Block deletion with `409 Conflict` when the category is referenced by existing transactions or budgets. |
| 5 | What is the maximum allowed page size? | 25 records per page. |
| 6 | Is a token refresh mechanism required, or is re-login on token expiry acceptable for the initial release? | Re-login on JWT expiry is acceptable — no dedicated refresh-token endpoint needed for the initial release. |
| 7 | Is a user profile management endpoint needed (e.g., change password, update display name, delete account)? | Yes — include user profile management endpoints for change password, update display name, and delete account. |
| 8 | Should default categories be seeded via a database migration tool or hardcoded as an enum? | Use Liquibase for database migrations and seeding default categories. |
| 9 | Are there specific JWT token expiration time requirements? | 2 hours. |
| 10 | What are the target response time SLAs? | p95 under 500ms. |
| 11 | What is the expected number of concurrent users or requests per second for initial production capacity planning? | No specific requirement for the initial release. |
| 12 | Should contributions be individually listed and deletable per goal, or is a contribution a simple additive operation with no per-entry history exposed? | Simple additive operation — no per-entry contribution history exposed via the API. |
| 13 | Are additional report types needed for the initial release? | Yes — add a monthly trend report providing a month-year breakdown of income, expenses, and net balance. |
| 14 | Which migration tool is preferred — Flyway or Liquibase? | Liquibase (confirmed). |
| 15 | Is multi-currency support required? | No — single currency only for the initial release. |

---

## Review & Acceptance Checklist

### Content Quality
- [x] All mandatory sections are present and fully filled
- [x] No implementation details bleed into requirement statements
- [x] Requirements are written for developers and product stakeholders
- [x] All `[NEEDS CLARIFICATION]` markers resolved

### Requirement Completeness
- [x] No `[NEEDS CLARIFICATION]` markers remain
- [x] All functional requirements are testable and unambiguous
- [x] API endpoint contract is fully defined
- [x] Entity data models are specified
- [x] Error scenarios are documented
- [x] Security requirements are defined
- [x] Performance targets are measurable
- [x] Scope is clearly bounded
- [x] Assumptions are documented

### Foundation Alignment
- [ ] BigDecimal used exclusively for monetary values (NFR-010 to NFR-012)
- [ ] Layered architecture respected (Controller / Service / Repository)
- [ ] DTO pattern enforced at API boundary
- [ ] JWT stateless authentication required (NFR-001)
- [ ] Pagination required on all collection endpoints
- [ ] SpringDoc OpenAPI annotations required on all public endpoints
- [ ] Test coverage thresholds met (NFR-043)
- [ ] Java 17 records used for DTOs (NFR-051)
- [ ] Static analysis integrated into build (NFR-054)
