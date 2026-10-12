# Finance Tracker API — Implementation Plan

**Specification**: [spec.md](spec.md)
**Branch**: `feature/finance-tracker-api`
**Created**: 2026-03-09
**Status**: Ready for Review
**Estimated Complexity**: High
**Foundation Version**: 1.0.0
**Foundation Type**: default (Spring Boot 3.x / Java 17 REST API)

---

## Summary

This plan describes the technical approach and phased implementation strategy for the Finance
Tracker REST API. The API enables authenticated users to manage personal financial data
including transactions, categories, budgets, savings goals, and financial reports. The system
is built on Java 17 and Spring Boot 3.x, follows a strict three-tier layered architecture,
uses JWT-based stateless authentication, stores all monetary values as `BigDecimal`, and
manages database schema migrations via Liquibase. The implementation is divided into six
sequential phases with clear gates and deliverables.

---

## Technical Context

| Concern | Choice | Details |
|---|---|---|
| Language | Java | 17 (LTS) |
| Framework | Spring Boot | 3.x latest stable |
| Build Tool | Maven | `pom.xml`; Java 17 toolchain, JaCoCo, Checkstyle, SpotBugs, MapStruct annotation processor |
| Persistence | Spring Data JPA + Hibernate | ORM provider |
| Database (dev/prod) | PostgreSQL | Configured via Spring profiles |
| Database (test) | H2 | In-memory, `@DataJpaTest` scope |
| Schema Migrations | Liquibase | Default category seeding included |
| Validation | Jakarta Bean Validation | Constraints on DTO fields |
| Authentication | Spring Security + JWT | Stateless; 2-hour token expiry |
| JWT Library | `com.auth0:java-jwt` | Single dependency; produces and validates signed JWT access tokens |
| Password Hashing | BCrypt | Via `BCryptPasswordEncoder` |
| Mapping | MapStruct | Entity ↔ DTO mapping |
| API Documentation | SpringDoc OpenAPI 3 (Swagger UI) | Auto-generated from annotations |
| Testing | JUnit 5 + Mockito + Spring Boot Test | Unit, integration, repository layers |
| Static Analysis | Checkstyle + SpotBugs | Integrated into build |
| Architecture Tests | ArchUnit | Layer dependency enforcement |
| API Prefix | `/api/v1/` | All endpoints |
| Performance Target | p95 < 500ms | Under normal load |
| Page Size | Default 20; max 25 | Enforced at controller level |
| Currency | Single currency | Multi-currency out of scope |
| Token Refresh | Re-login on expiry | No refresh-token endpoint |

---

## Foundation Compliance Check

Based on `directive/foundation.md` v1.0.0. This gate must be reviewed before Phase 1 begins
and re-verified after Phase 3.

### Principle 1: Financial Data Integrity First
- [ ] All monetary fields use `BigDecimal` with `precision=19, scale=4`
- [ ] `double` and `float` are prohibited in all financial model classes
- [ ] All write operations on financial state wrapped in `@Transactional`
- [ ] Rounding mode is `BigDecimal.HALF_UP` throughout

### Principle 2: Layered Separation of Concerns
- [ ] Classes reside in appropriate layer packages: `controller`, `service`, `repository`, `model`, `dto`, `mapper`, `config`, `exception`
- [ ] Service classes carry `@Service`; no business logic in controllers
- [ ] Controller classes carry `@RestController`; no business logic
- [ ] Repository interfaces extend `JpaRepository` or `CrudRepository`
- [ ] Cross-layer dependencies flow downward only (Controller → Service → Repository)

### Principle 3: Test Coverage as a First-Class Requirement
- [ ] All service-layer methods have JUnit 5 + Mockito unit tests
- [ ] All controller endpoints have `@SpringBootTest` + `MockMvc` integration tests
- [ ] Repository layer tested with `@DataJpaTest` against H2
- [ ] Overall line coverage >= 80%; branch coverage target >= 70%
- [ ] Tests are deterministic (no external services, no mutable shared state)

### Principle 4: Explicit, Consistent API Contracts
- [ ] Every public endpoint annotated with `@Operation`, `@ApiResponse`, `@Schema`
- [ ] All collection endpoints support Spring Data `Pageable` parameters
- [ ] Error responses conform to `{ timestamp, status, error, message, path }` schema
- [ ] Validation error responses include optional `fieldErrors` array

### Principle 5: Security by Default
- [ ] All endpoints require JWT authentication except `/api/v1/auth/register` and `/api/v1/auth/login`
- [ ] Passwords stored as BCrypt hashes only; never logged or returned
- [ ] JWT secret externalized via environment variable
- [ ] CORS explicitly configured; no wildcard origins in production profile
- [ ] Spring Security integration test asserts 401 for unauthenticated requests

### Principle 6: Clean Code and Java 17 Idioms
- [ ] Code compiles without `-Xlint:all` warnings (or suppressions are documented)
- [ ] Java 17 records used for all DTO and value types
- [ ] Method length <= 30 lines; class length <= 300 lines
- [ ] All public service and controller methods carry Javadoc
- [ ] `var` used only where inferred type is unambiguous

**Violations Requiring Justification**: None anticipated. Any violations discovered during
implementation must be documented in the Complexity Tracking section with rationale.

---

## Project Structure

```
finance-tracker/
├── src/
│   ├── main/
│   │   ├── java/com/financetracker/
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java          # Spring Security + JWT filter chain
│   │   │   │   ├── CorsConfig.java              # Explicit CORS configuration
│   │   │   │   ├── OpenApiConfig.java           # SpringDoc / Swagger UI config
│   │   │   │   └── PaginationConfig.java        # Default page size, max page size enforcement
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java          # /api/v1/auth/*
│   │   │   │   ├── UserController.java          # /api/v1/users/me
│   │   │   │   ├── TransactionController.java   # /api/v1/transactions
│   │   │   │   ├── CategoryController.java      # /api/v1/categories
│   │   │   │   ├── BudgetController.java        # /api/v1/budgets
│   │   │   │   ├── GoalController.java          # /api/v1/goals
│   │   │   │   └── ReportController.java        # /api/v1/reports
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── TransactionService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── BudgetService.java
│   │   │   │   ├── GoalService.java
│   │   │   │   └── ReportService.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── BudgetRepository.java
│   │   │   │   └── GoalRepository.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── Budget.java
│   │   │   │   ├── Goal.java
│   │   │   │   ├── TransactionType.java         # Enum: INCOME, EXPENSE
│   │   │   │   └── GoalStatus.java             # Enum: IN_PROGRESS, ACHIEVED
│   │   │   ├── dto/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── RegisterRequest.java     # Java 17 record
│   │   │   │   │   ├── LoginRequest.java        # Java 17 record
│   │   │   │   │   └── TokenResponse.java       # Java 17 record
│   │   │   │   ├── user/
│   │   │   │   │   ├── UserProfileResponse.java # Java 17 record
│   │   │   │   │   ├── UpdateProfileRequest.java
│   │   │   │   │   └── ChangePasswordRequest.java
│   │   │   │   ├── transaction/
│   │   │   │   │   ├── TransactionRequest.java
│   │   │   │   │   ├── TransactionResponse.java # Java 17 record
│   │   │   │   │   └── TransactionFilterParams.java
│   │   │   │   ├── category/
│   │   │   │   │   ├── CategoryRequest.java
│   │   │   │   │   └── CategoryResponse.java    # Java 17 record
│   │   │   │   ├── budget/
│   │   │   │   │   ├── BudgetRequest.java
│   │   │   │   │   └── BudgetResponse.java      # Java 17 record (includes spentAmount, overLimit)
│   │   │   │   ├── goal/
│   │   │   │   │   ├── GoalRequest.java
│   │   │   │   │   ├── GoalResponse.java        # Java 17 record (includes computed fields)
│   │   │   │   │   └── ContributionRequest.java
│   │   │   │   └── report/
│   │   │   │       ├── SummaryResponse.java
│   │   │   │       ├── CategoryBreakdown.java   # Java 17 record
│   │   │   │       └── MonthlyTrendResponse.java
│   │   │   ├── mapper/
│   │   │   │   ├── UserMapper.java
│   │   │   │   ├── TransactionMapper.java
│   │   │   │   ├── CategoryMapper.java
│   │   │   │   ├── BudgetMapper.java
│   │   │   │   └── GoalMapper.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java  # @ControllerAdvice
│   │   │   │   ├── ErrorResponse.java           # Structured error DTO (Java 17 record)
│   │   │   │   ├── ResourceNotFoundException.java
│   │   │   │   ├── ForbiddenOperationException.java
│   │   │   │   ├── DuplicateResourceException.java
│   │   │   │   └── ReferentialIntegrityException.java
│   │   │   └── security/
│   │   │       ├── JwtService.java              # Token generation and validation
│   │   │       ├── JwtAuthenticationFilter.java # OncePerRequestFilter
│   │   │       └── UserDetailsServiceImpl.java  # Loads UserDetails from DB
│   │   └── resources/
│   │       ├── application.yml                  # Base config
│   │       ├── application-dev.yml              # Dev profile (PostgreSQL)
│   │       ├── application-test.yml             # Test profile (H2)
│   │       ├── application-prod.yml             # Prod profile (env-var-driven)
│   │       └── db/changelog/
│   │           ├── db.changelog-master.xml      # Liquibase root changeset
│   │           ├── 001-create-users.xml
│   │           ├── 002-create-categories.xml
│   │           ├── 003-seed-default-categories.xml
│   │           ├── 004-create-transactions.xml
│   │           ├── 005-create-budgets.xml
│   │           └── 006-create-goals.xml
│   └── test/
│       └── java/com/financetracker/
│           ├── architecture/
│           │   └── ArchitectureTest.java        # ArchUnit layer rules
│           ├── controller/
│           │   ├── AuthControllerTest.java
│           │   ├── UserControllerTest.java
│           │   ├── TransactionControllerTest.java
│           │   ├── CategoryControllerTest.java
│           │   ├── BudgetControllerTest.java
│           │   ├── GoalControllerTest.java
│           │   └── ReportControllerTest.java
│           ├── service/
│           │   ├── AuthServiceTest.java
│           │   ├── UserServiceTest.java
│           │   ├── TransactionServiceTest.java
│           │   ├── CategoryServiceTest.java
│           │   ├── BudgetServiceTest.java
│           │   ├── GoalServiceTest.java
│           │   └── ReportServiceTest.java
│           ├── repository/
│           │   ├── TransactionRepositoryTest.java
│           │   ├── CategoryRepositoryTest.java
│           │   ├── BudgetRepositoryTest.java
│           │   └── GoalRepositoryTest.java
│           └── security/
│               └── SecurityIntegrationTest.java # 401 enforcement on protected endpoints
```

---

## Implementation Phases

### Phase 1: Project Scaffolding and Infrastructure

**Objective**: Establish the project skeleton, build configuration, database infrastructure,
and cross-cutting concerns so that all subsequent phases have a stable foundation to build on.

**Prerequisites**: None. This is the starting point.

**Tasks**:

1. Initialize a Spring Boot 3.x project via Spring Initializr with dependencies:
   Spring Web, Spring Data JPA, Spring Security, Liquibase Migration, Validation,
   SpringDoc OpenAPI, MapStruct, Lombok (if adopted), H2 (test scope), PostgreSQL driver.
2. Configure `pom.xml` with:
   - Java 17 toolchain (`maven-compiler-plugin` source/target set to `17`)
   - `maven-checkstyle-plugin` pointing to `config/checkstyle.xml`
   - `com.github.spotbugs:spotbugs-maven-plugin`
   - `jacoco-maven-plugin` with a `check` goal enforcing 80% line coverage as a build-breaking gate
   - MapStruct annotation processor declared in `annotationProcessorPaths` of `maven-compiler-plugin`
3. Create `application.yml`, `application-dev.yml`, `application-test.yml`, and
   `application-prod.yml` with placeholders for database URL, JWT secret, and CORS origins
   using Spring profile conventions. JWT secret and database credentials MUST be bound to
   environment variables in dev and prod profiles.
4. Create the Liquibase changelog master file and individual changeset files:
   - `001-create-users.xml`: `users` table (`id`, `email`, `password_hash`, `display_name`, `created_at`)
   - `002-create-categories.xml`: `categories` table (`id`, `name`, `is_system_default`, `user_id`)
   - `003-seed-default-categories.xml`: Insert the nine default categories
     (Food, Transport, Utilities, Salary, Entertainment, Healthcare, Housing, Education, Other)
     with `is_system_default = true` and `user_id = NULL`
   - `004-create-transactions.xml`: `transactions` table with `amount DECIMAL(19,4)`,
     `type`, `category_id`, `user_id`, `date`, `description`, `created_at`, `updated_at`
   - `005-create-budgets.xml`: `budgets` table with `limit_amount DECIMAL(19,4)`, `month`,
     `year`, unique constraint on `(user_id, category_id, month, year)`, `@Version` column
     for optimistic locking
   - `006-create-goals.xml`: `goals` table with `target_amount DECIMAL(19,4)`,
     `contributed_amount DECIMAL(19,4)`, `target_date`, `status`, `created_at`
5. Configure Checkstyle rules (`config/checkstyle.xml`) to flag `double` / `float` usage
   in financial model classes and enforce method and class length limits.
6. Implement the global `@ControllerAdvice` exception handler (`GlobalExceptionHandler`)
   with the `ErrorResponse` record matching the schema
   `{ timestamp, status, error, message, path }` and optional `fieldErrors` array.
7. Implement the `PaginationConfig` or a custom `HandlerMethodArgumentResolver` that
   enforces `size <= 25` and defaults `page=0, size=20`. Requests with `size > 25` return
   `400 Bad Request` with a structured error response.
8. Write the ArchUnit architecture test (`ArchitectureTest.java`) asserting:
   - Controllers do not depend on repository classes directly
   - Services do not depend on HTTP types
   - Repositories are only called from service layer
   - All classes are in their designated packages

**Deliverable**: Compilable project with verified Liquibase migrations, H2 test profile
running cleanly, build pipeline enforcing coverage and static analysis, and architecture
test suite passing on the empty skeleton.

**Gate**: Build passes with zero static analysis violations. Liquibase migrations apply
cleanly against H2. ArchUnit tests pass.

---

### Phase 2: Security and Authentication

**Objective**: Implement JWT-based stateless authentication (register, login) and user
profile management so that all subsequent phases can build on a working, tested security
layer.

**Prerequisites**: Phase 1 complete.

**Tasks**:

1. Implement `User` JPA entity (id, email, passwordHash, displayName, createdAt) with
   `@Column(unique=true)` on email and `@Column(nullable=false)` guards. No `double` or
   `float` fields.
2. Implement `UserRepository` extending `JpaRepository<User, Long>` with a
   `findByEmail(String email): Optional<User>` query method.
3. Add the `com.auth0:java-jwt` dependency to `pom.xml`:
   ```xml
   <dependency>
       <groupId>com.auth0</groupId>
       <artifactId>java-jwt</artifactId>
   </dependency>
   ```
   Pin the version via a `<auth0-jwt.version>` property in the `<properties>` block; use the
   latest stable release of `com.auth0:java-jwt`.
4. Implement `JwtService` with methods `generateToken(UserDetails)` and
   `validateToken(String, UserDetails)`. Use the Auth0 java-jwt APIs:
   `JWT.create()` for token creation and `JWT.require(algorithm)` for validation.
   The signing algorithm is instantiated via `Algorithm.HMAC256(secret)` where `secret` is
   read from `${app.jwt.secret}`. Token expiry is 2 hours
   (`${app.jwt.expiry-ms}` defaulting to `7200000`).
5. Implement `JwtAuthenticationFilter` (`OncePerRequestFilter`) that extracts the Bearer
   token from the `Authorization` header, validates it via `JwtService`, and populates
   the `SecurityContext`.
6. Implement `UserDetailsServiceImpl` that loads a `UserDetails` instance from
   `UserRepository` by email for use by Spring Security.
7. Implement `SecurityConfig` (`@Configuration`, `@EnableWebSecurity`) that:
   - Disables session creation (stateless)
   - Permits `/api/v1/auth/**` without authentication
   - Requires authentication on all other paths
   - Registers `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`
   - Configures explicit CORS (origins read from `${app.cors.allowed-origins}`)
8. Implement `AuthService` with:
   - `register(RegisterRequest)`: validates email uniqueness, hashes password with
     `BCryptPasswordEncoder`, persists `User`, returns success response
   - `login(LoginRequest)`: verifies credentials, returns `TokenResponse` with
     `{ token, expiresIn }`
9. Implement `AuthController` (`POST /api/v1/auth/register`, `POST /api/v1/auth/login`)
   with SpringDoc annotations. Registration returns `201 Created`.
10. Implement `UserService` with:
    - `getProfile(Long userId)`: returns `UserProfileResponse`
    - `updateDisplayName(Long userId, UpdateProfileRequest)`: updates display name
    - `changePassword(Long userId, ChangePasswordRequest)`: validates current password,
      hashes and stores new password
    - `deleteAccount(Long userId)`: deletes user and all owned data
11. Implement `UserController` (`GET`, `PUT /api/v1/users/me`, `PUT /api/v1/users/me/password`,
    `DELETE /api/v1/users/me`) with SpringDoc annotations. Password change and delete return
    `204 No Content`.
12. Write unit tests for `AuthService` and `UserService` (JUnit 5 + Mockito).
13. Write `@SpringBootTest` + `MockMvc` integration tests for `AuthController` and
    `UserController` covering happy paths and validation failures.
14. Write `SecurityIntegrationTest` asserting that all protected endpoints return `401` for
    requests without a valid JWT.
15. Write `@DataJpaTest` tests for `UserRepository`.

**Deliverable**: Working registration, login, and user profile management. JWT filter
enforces authentication on all protected routes. All 401 assertions pass.

**Gate**: Security integration tests pass. Authentication integration tests pass.
`AuthService` and `UserService` unit tests pass at >= 80% line coverage.

---

### Phase 3: Category Management

**Objective**: Implement category listing, creation, and deletion so that transaction and
budget phases (which depend on category IDs) have a working data foundation.

**Prerequisites**: Phase 2 complete (authenticated user context available).

**Tasks**:

1. Implement `Category` JPA entity (id, name, isSystemDefault, user). When `user` is null,
   the category is a system default.
2. Implement `CategoryRepository` with:
   - `findByUserIsNullOrUserId(Long userId)`: returns system defaults and user's own categories
   - `existsByNameAndUserId(String name, Long userId)`: for duplicate name check
   - `existsByIdAndUserIsNull(Long id)`: to identify system defaults on delete
   - `existsByIdAndTransactionsNotEmptyOrBudgetsNotEmpty(Long id)`: referential integrity check
     (or equivalent JPQL query to count references)
3. Implement `CategoryService` with:
   - `getCategories(Long userId)`: returns all system defaults plus user's custom categories
   - `createCategory(Long userId, CategoryRequest)`: checks name uniqueness for the user,
     creates the category. Returns `409 Conflict` if duplicate name.
   - `deleteCategory(Long userId, Long categoryId)`: rejects with `403 Forbidden` if
     system default; rejects with `409 Conflict` if referenced by transactions or budgets;
     otherwise deletes.
4. Implement `CategoryMapper` (entity ↔ `CategoryResponse` record).
5. Implement `CategoryController` (`GET /api/v1/categories`, `POST /api/v1/categories`,
   `DELETE /api/v1/categories/{id}`) with SpringDoc annotations.
6. Write unit tests for `CategoryService` (happy paths and all error branches).
7. Write integration tests for `CategoryController`.
8. Write `@DataJpaTest` tests for `CategoryRepository`.

**Deliverable**: Fully functional category endpoints. System defaults are seeded by
Liquibase and returned alongside user-created categories. Deletion guards work correctly.

**Gate**: All category service unit tests pass. All category controller integration tests
pass. System-default categories are present and protected from deletion.

---

### Phase 4: Transaction Management

**Objective**: Implement CRUD operations for transactions with filtering and pagination,
enforcing ownership scoping and financial data integrity rules.

**Prerequisites**: Phase 3 complete (categories available for FK association).

**Tasks**:

1. Implement `Transaction` JPA entity with `@Column(precision=19, scale=4)` on `amount`,
   `TransactionType` enum, FK to `User` and `Category`, `LocalDate` for `date`, optional
   `description` (max 500 chars), `createdAt`, `updatedAt`. Annotate `updatedAt` with
   `@PreUpdate`.
2. Implement `TransactionRepository` with:
   - `findByUserIdOrderByDateDesc(Long userId, Pageable pageable)`: default paginated list
   - A custom `@Query` or `Specification`-based method supporting filters:
     `type`, `categoryId`, `startDate`, `endDate` — all optional, combinable
3. Implement `TransactionService` with:
   - `createTransaction(Long userId, TransactionRequest)`: validates category ownership
     (user owns the category or it is a system default), persists with `@Transactional`
   - `getTransactions(Long userId, TransactionFilterParams, Pageable)`: paginated,
     filtered list of own transactions
   - `getTransaction(Long userId, Long id)`: returns own transaction; throws
     `ForbiddenOperationException` if the transaction belongs to another user
   - `updateTransaction(Long userId, Long id, TransactionRequest)`: updates own transaction
   - `deleteTransaction(Long userId, Long id)`: deletes own transaction
   All write operations annotated with `@Transactional`.
4. Implement `TransactionMapper` (entity ↔ `TransactionResponse` record).
5. Implement `TransactionController` with all five endpoints, SpringDoc annotations,
   `Pageable` parameters on the list endpoint, and filter query parameters.
6. Write unit tests for `TransactionService` covering:
   - Successful creation, retrieval, update, delete
   - Access to another user's transaction returns `403 Forbidden`
   - Negative or zero amount returns `400 Bad Request`
   - Future-dated transaction is accepted
7. Write integration tests for `TransactionController` covering pagination defaults,
   max page size enforcement, filter combinations, and the 403/400 error paths.
8. Write `@DataJpaTest` tests for `TransactionRepository` filter queries.

**Deliverable**: Full transaction CRUD with ownership isolation, filtering, pagination,
and BigDecimal integrity.

**Gate**: All transaction service unit tests pass. All transaction controller integration
tests pass. Coverage >= 80% for the transaction package.

---

### Phase 5: Budget and Goal Management

**Objective**: Implement budget and goal features including computed fields (`spentAmount`,
`overLimit`, goal progress metrics) and goal contribution handling.

**Prerequisites**: Phase 4 complete (transaction data available for `spentAmount`
computation).

**Tasks**:

#### Budget

1. Implement `Budget` JPA entity with `@Column(precision=19, scale=4)` on `limitAmount`,
   FK to `User` and `Category`, `month` (1–12), `year`, and a `@Version` field for
   optimistic locking. Add `@Table(uniqueConstraints = @UniqueConstraint(columnNames =
   {"user_id", "category_id", "month", "year"}))`.
2. Implement `BudgetRepository` with:
   - `findByUserIdWithSpentAmount(Long userId)`: a `@Query` that joins the transactions
     table to compute the sum of EXPENSE amounts per budget period, returning
     `BudgetProjection` or a native tuple
   - `existsByUserIdAndCategoryIdAndMonthAndYear(...)`: duplicate budget check
3. Implement `BudgetService` with:
   - `createBudget(Long userId, BudgetRequest)`: validates uniqueness per
     (user, category, month, year); returns `409 Conflict` on duplicate
   - `getBudgets(Long userId)`: returns list including computed `spentAmount` and
     `overLimit` fields
   - `getBudget(Long userId, Long id)`: returns single budget with computed fields
   - `updateBudget(Long userId, Long id, BudgetRequest)`: updates `limitAmount`
   - `deleteBudget(Long userId, Long id)`: deletes budget
   All write operations annotated with `@Transactional`.
4. Implement `BudgetMapper` mapping entity + computed amounts to `BudgetResponse` record.
5. Implement `BudgetController` with all five endpoints and SpringDoc annotations.

#### Goal

6. Implement `Goal` JPA entity with `@Column(precision=19, scale=4)` on `targetAmount`
   and `contributedAmount` (default `BigDecimal.ZERO`), `GoalStatus` enum, `LocalDate`
   for `targetDate`, and `createdAt`.
7. Implement `GoalRepository` (standard `JpaRepository<Goal, Long>` with
   `findByUserId(Long userId)`).
8. Implement `GoalService` with:
   - `createGoal(Long userId, GoalRequest)`: persists new goal with status `IN_PROGRESS`
   - `getGoals(Long userId)`: returns list with computed response fields
   - `getGoal(Long userId, Long id)`: returns single goal with computed response fields
   - `updateGoal(Long userId, Long id, GoalRequest)`: updates name, targetAmount, targetDate
   - `deleteGoal(Long userId, Long id)`: deletes goal
   - `addContribution(Long userId, Long id, ContributionRequest)`: adds positive
     `amount` to `contributedAmount`; sets status to `ACHIEVED` when
     `contributedAmount >= targetAmount`. All wrapped in `@Transactional`.
   Computed response fields: `remainingAmount = max(0, targetAmount - contributedAmount)`,
   `progressPercentage = (contributedAmount / targetAmount) * 100` rounded to 2 decimal
   places using `HALF_UP`, `status` derived from the comparison.
9. Implement `GoalMapper` mapping entity to `GoalResponse` record.
10. Implement `GoalController` with all six endpoints (including the contributions endpoint)
    and SpringDoc annotations.
11. Write unit tests for `BudgetService` and `GoalService` covering:
    - Happy paths for all operations
    - `overLimit` logic (spentAmount equals, exceeds, and is below limitAmount)
    - `ACHIEVED` status transition on contribution meeting target
    - Duplicate budget returns `409 Conflict`
    - BigDecimal arithmetic precision (no rounding errors)
12. Write integration tests for `BudgetController` and `GoalController`.
13. Write `@DataJpaTest` tests for `BudgetRepository` spend computation query.

**Deliverable**: Fully functional budget and goal endpoints with correct computed fields,
status transitions, and optimistic locking on budgets.

**Gate**: All budget and goal service unit tests pass. Computed field assertions pass.
`ACHIEVED` status transition is verified. Optimistic locking path is tested.

---

### Phase 6: Reporting, OpenAPI Finalization, and Quality Gate

**Objective**: Implement the two reporting endpoints, finalize OpenAPI documentation across
all controllers, enforce static analysis and coverage gates, and perform end-to-end
validation.

**Prerequisites**: Phases 1–5 complete.

**Tasks**:

#### Reporting

1. Implement `ReportService` with:
   - `getSummary(Long userId, LocalDate startDate, LocalDate endDate)`: if no dates are
     supplied, defaults to the first and last day of the current calendar month. Executes
     aggregation queries to produce:
     - Total income (sum of INCOME transactions in range)
     - Total expenses (sum of EXPENSE transactions in range)
     - Net balance (income minus expenses)
     - Per-category breakdown of income and expense totals
     All values as `BigDecimal` with `HALF_UP` rounding.
   - `getMonthlyTrend(Long userId, LocalDate startDate, LocalDate endDate)`: groups
     transactions by year-month and returns total income, total expenses, and net balance
     per month. Results ordered by year-month ascending.
2. Implement `ReportRepository` queries (or extend `TransactionRepository`) using JPQL
   or Spring Data projections for the aggregation operations. Raw SQL is permitted only if
   JPQL is insufficient (per foundation).
3. Implement `ReportController` (`GET /api/v1/reports/summary`,
   `GET /api/v1/reports/monthly-trend`) with SpringDoc annotations. Both endpoints accept
   optional `startDate` and `endDate` query parameters as `LocalDate`.
4. Write unit tests for `ReportService` including default date range logic and
   per-category breakdown correctness.
5. Write integration tests for `ReportController` covering no-date defaults, custom ranges,
   and empty result sets.

#### OpenAPI Finalization

6. Review all controllers to ensure every endpoint carries `@Operation(summary=...)`,
   `@ApiResponse` annotations for all documented status codes, and `@Schema` on all
   DTO fields. Verify Swagger UI renders all 27 endpoints correctly.
7. Add an OpenAPI integration test that loads the generated spec and asserts:
   - All expected paths exist
   - Error response shape matches `{ timestamp, status, error, message, path }`

#### Quality Gate

8. Run the full build with JaCoCo coverage report. Confirm overall line coverage >= 80%
   and branch coverage >= 70%. Address any gaps.
9. Run Checkstyle. Confirm zero violations or document any `@SuppressWarnings` with
   justification comments.
10. Run SpotBugs. Confirm no high-priority findings.
11. Run ArchUnit tests. Confirm all layer dependency rules pass.
12. Perform a manual walkthrough of the error response schema for at least one validation
    failure and one `404` per resource to confirm the structured error format.

**Deliverable**: Fully implemented and tested API with all 27 endpoints covered, OpenAPI
spec generated and validated, static analysis clean, coverage gates passing.

**Gate**: Full build passes including JaCoCo gate (>= 80% line coverage), Checkstyle,
SpotBugs, and ArchUnit. All integration tests pass. Swagger UI renders all endpoints.

---

## Dependencies and Sequencing

```
Phase 1 (Scaffolding)
    ↓
Phase 2 (Security & Auth)
    ↓
Phase 3 (Categories)         ← Phase 4 and 5 depend on categories for FK references
    ↓
Phase 4 (Transactions)       ← Phase 5 budget service depends on transaction data for spentAmount
    ↓
Phase 5 (Budgets & Goals)    ← Phase 6 reporting depends on transaction aggregates
    ↓
Phase 6 (Reports & Quality Gate)
```

No phases may be parallelized in the primary dependency chain. Within each phase, test
writing for one component can begin in parallel with implementation of the next component
in the same phase.

---

## Risk Assessment

### High Risk

| Risk | Impact | Likelihood | Mitigation |
|---|---|---|---|
| `spentAmount` computation accuracy | Incorrect budget totals visible to users | Medium | Use JPQL aggregate query with explicit `BigDecimal` precision; add integration test asserting exact values after known transactions are inserted |
| JWT secret exposure | Security breach | Low | Enforce environment variable binding in all non-test profiles; add CI repository secrets scanning |
| Optimistic locking conflicts on budgets | `409 OptimisticLockException` visible to users | Low | Map `OptimisticLockException` in `GlobalExceptionHandler` to `409 Conflict` with descriptive message |

### Medium Risk

| Risk | Impact | Likelihood | Mitigation |
|---|---|---|---|
| Pagination max-size enforcement gap | Requests with `size > 25` return large data sets | Medium | Implement a `PageableHandlerMethodArgumentResolver` customizer or a `@ControllerAdvice` that validates `Pageable` parameters before reaching service layer; add dedicated test |
| Category referential integrity check performance | Slow delete checks on large datasets | Low | Add database indexes on `transaction.category_id` and `budget.category_id`; include in Liquibase changesets |
| BigDecimal division by zero in goal progress | `ArithmeticException` at runtime | Low | Guard `progressPercentage` calculation: return `0.00` when `targetAmount` is zero (though DTO validation should prevent this; add defensive check) |
| Cascading deletes on `DELETE /api/v1/users/me` | Orphaned records | Medium | Define `CascadeType.ALL` + `orphanRemoval=true` on all `User` associations, or implement explicit deletion order in `UserService.deleteAccount` |

### Low Risk

| Risk | Impact | Likelihood | Mitigation |
|---|---|---|---|
| Liquibase migration conflicts in team development | Build failures | Low | Use sequential numeric prefixes for changeset IDs; never edit applied changesets |
| MapStruct version incompatibility with Java 17 records | Compilation failures | Low | Pin MapStruct to a version confirmed compatible with Java 17 records; test on project init |
| H2 dialect differences from PostgreSQL | Tests pass but production fails | Low | Use `spring.jpa.database-platform=org.hibernate.dialect.H2Dialect` for test profile only; run schema validation against PostgreSQL in CI |

---

## Testing Strategy

### Unit Tests (JUnit 5 + Mockito)

- Scope: All service-layer classes
- All repository and HTTP dependencies are mocked
- Each test class covers one service; tests are independent and deterministic
- Key assertions: `BigDecimal` equality using `compareTo`, not `equals`; status codes and
  exception types; computed field values (spentAmount, overLimit, progressPercentage, status)

### Integration Tests (@SpringBootTest + MockMvc)

- Scope: All controller-layer endpoints
- Uses `@SpringBootTest(webEnvironment = RANDOM_PORT)` with `MockMvc`
- Test profile activates H2 and skips external dependencies
- Covers: happy paths, validation failures (400), authentication failures (401),
  authorization failures (403), not-found paths (404), conflict paths (409)
- Pagination: asserts default page size (20) and max page size rejection (> 25 → 400)
- Error response schema assertions on every error path

### Repository Tests (@DataJpaTest)

- Scope: All repository interfaces
- Uses H2 in-memory database with Liquibase migrations applied
- Tests custom `@Query` methods: filter combinations, aggregate queries, uniqueness checks
- Asserts `BigDecimal` round-trip precision through the persistence layer

### Security Tests

- `SecurityIntegrationTest` sends unauthenticated requests to all protected endpoints
- Asserts `401 Unauthorized` for every protected route
- Asserts `200`/`201`/`204` for public auth routes

### Architecture Tests (ArchUnit)

- Asserts Controller → Service → Repository dependency direction only
- Asserts no HTTP types in service layer
- Asserts all classes in correct packages by naming convention

### Coverage Gate

- JaCoCo enforces >= 80% line coverage and >= 70% branch coverage as a build-breaking gate
- Coverage report published as a CI artifact on every pull request

---

## Complexity Tracking

No foundation principle violations are anticipated. The following design decisions represent
complexity trade-offs that are aligned with the foundation:

| Decision | Justification |
|---|---|
| `spentAmount` computed at query time | Avoids denormalization and cache invalidation complexity; acceptable given the p95 < 500ms SLA and single-user data scope |
| Optimistic locking (`@Version`) on `Budget` | Required by spec to prevent concurrent update corruption; maps to a standard Spring Data pattern with no foundation violation |
| `deleteAccount` requires explicit cascade or ordered deletion | Ensures no orphaned data; `CascadeType.ALL` with `orphanRemoval` is the idiomatic JPA approach |
| No per-entry `GoalContribution` history table | Explicitly scoped out in specification; simple additive model reduces schema complexity |

---

## Progress Tracking

**Phase Status**:
- [ ] Phase 1: Project Scaffolding and Infrastructure
- [ ] Phase 2: Security and Authentication
- [ ] Phase 3: Category Management
- [ ] Phase 4: Transaction Management
- [ ] Phase 5: Budget and Goal Management
- [ ] Phase 6: Reporting, OpenAPI Finalization, and Quality Gate

**Gate Status**:
- [ ] Initial Foundation Compliance Check: PASS
- [ ] Phase 1 Gate: Build, migrations, ArchUnit
- [ ] Phase 2 Gate: Auth tests, Security 401 tests
- [ ] Phase 3 Gate: Category tests, deletion guards
- [ ] Phase 4 Gate: Transaction tests, filter/pagination
- [ ] Phase 5 Gate: Budget/goal tests, computed fields, optimistic locking
- [ ] Phase 6 Gate: Full coverage gate, static analysis, OpenAPI validation

**Quality Gates**:
- [ ] Line coverage >= 80% (JaCoCo build gate)
- [ ] Branch coverage >= 70%
- [ ] Checkstyle: zero violations
- [ ] SpotBugs: zero high-priority findings
- [ ] ArchUnit: all layer rules pass
- [ ] All 27 endpoints documented in Swagger UI

---

## Clarifications Received

All clarification questions have been resolved. The answers below have been incorporated
throughout the plan.

| # | Question | Answer |
|---|---|---|
| 1 | Which build tool — Maven or Gradle — should be used? | Maven (`pom.xml`) |
| 2 | Which JWT library should be used — `io.jsonwebtoken` (JJWT) or `com.auth0:java-jwt`? | `com.auth0:java-jwt` — single dependency, no split api/impl/jackson artifacts required |

---

*Based on Foundation v1.0.0 — See `directive/foundation.md` for complete principles and governance.*
