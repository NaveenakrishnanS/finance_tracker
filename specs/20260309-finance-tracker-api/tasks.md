<!--
TASKS DOCUMENT
Feature: Finance Tracker API
Plan: specs/20260309-finance-tracker-api/plan.md
Spec: specs/20260309-finance-tracker-api/spec.md
Foundation: directive/foundation.md v1.0.0
Generated: 2026-03-09
Status: Completed
-->

# Finance Tracker API — Implementation Tasks

**Feature Branch**: `feature/finance-tracker-api`
**Generated**: 2026-03-09
**Status**: Completed
**Foundation Version**: 1.0.0
**Foundation Type**: default (Spring Boot 3.x / Java 17 REST API)
**Total Tasks**: 77
**Parallel Tasks**: 34

---

## Table of Contents

1. [Category: Setup (Phase 1)](#category-setup-phase-1)
2. [Category: Tests — Architecture and Security (TDD First)](#category-tests--architecture-and-security-tdd-first)
3. [Category: Core — Security and Authentication (Phase 2)](#category-core--security-and-authentication-phase-2)
4. [Category: Tests — Security Integration (TDD)](#category-tests--security-integration-tdd)
5. [Category: Core — Category Management (Phase 3)](#category-core--category-management-phase-3)
6. [Category: Tests — Category (TDD)](#category-tests--category-tdd)
7. [Category: Core — Transaction Management (Phase 4)](#category-core--transaction-management-phase-4)
8. [Category: Tests — Transaction (TDD)](#category-tests--transaction-tdd)
9. [Category: Core — Budget Management (Phase 5)](#category-core--budget-management-phase-5)
10. [Category: Tests — Budget (TDD)](#category-tests--budget-tdd)
11. [Category: Core — Goal Management (Phase 5)](#category-core--goal-management-phase-5)
12. [Category: Tests — Goal (TDD)](#category-tests--goal-tdd)
13. [Category: Core — Reporting (Phase 6)](#category-core--reporting-phase-6)
14. [Category: Tests — Reporting (TDD)](#category-tests--reporting-tdd)
15. [Category: Polish — OpenAPI, Quality Gate, and Static Analysis](#category-polish--openapi-quality-gate-and-static-analysis)
16. [Dependency Graph](#dependency-graph)
17. [Parallel Execution Examples](#parallel-execution-examples)
18. [Validation Checklist](#validation-checklist)

---

## Category: Setup (Phase 1)

> Objective: Establish the project skeleton, build configuration, database infrastructure, and cross-cutting concerns.
> These tasks must complete before any implementation tasks begin.

---

### T001 — Initialize Spring Boot project via Spring Initializr

**Phase**: 1 — Project Scaffolding
**Parallel**: No (foundation for all other tasks)

Initialize a new Spring Boot 3.x Maven project with the following dependencies:
Spring Web, Spring Data JPA, Spring Security, Liquibase Migration, Validation,
SpringDoc OpenAPI 3, MapStruct, H2 (test scope), PostgreSQL Driver.

**Output file**: `finance-tracker/pom.xml`
**Output file**: `finance-tracker/src/main/java/com/financetracker/FinanceTrackerApplication.java`

---

### T002 — Configure pom.xml build plugins and toolchain

**Phase**: 1 — Project Scaffolding
**Parallel**: No (depends on T001)

Configure `pom.xml` with:
- Java 17 toolchain: set `maven-compiler-plugin` `source` and `target` to `17`
- `maven-checkstyle-plugin` pointing to `config/checkstyle.xml`
- `com.github.spotbugs:spotbugs-maven-plugin`
- `jacoco-maven-plugin` with a `check` goal enforcing >= 80% line coverage as a build-breaking gate
- MapStruct annotation processor declared in `annotationProcessorPaths` of `maven-compiler-plugin`
- `com.auth0:java-jwt` dependency pinned via `<auth0-jwt.version>` property in `<properties>` block

**Output file**: `finance-tracker/pom.xml`

---

### T003 — Create application configuration files for all Spring profiles [P]

**Phase**: 1 — Project Scaffolding
**Parallel**: Yes — independent of T002 content; can run alongside T004

Create the four YAML configuration files with the following requirements:
- `application.yml`: shared configuration, SpringDoc settings, pagination defaults (`page-size=20`, `max-page-size=25`), MapStruct config
- `application-dev.yml`: PostgreSQL datasource URL bound to environment variable `${DB_URL}`, credentials to `${DB_USERNAME}` / `${DB_PASSWORD}`, JWT secret to `${APP_JWT_SECRET}`
- `application-test.yml`: H2 in-memory datasource, `spring.jpa.database-platform=org.hibernate.dialect.H2Dialect`, Liquibase enabled, JWT secret as a fixed test value
- `application-prod.yml`: all sensitive values bound to environment variables; CORS origins bound to `${APP_CORS_ALLOWED_ORIGINS}`; no hardcoded credentials

**Output files**:
- `finance-tracker/src/main/resources/application.yml`
- `finance-tracker/src/main/resources/application-dev.yml`
- `finance-tracker/src/main/resources/application-test.yml`
- `finance-tracker/src/main/resources/application-prod.yml`

---

### T004 — Create Liquibase changelog master and users table changeset [P]

**Phase**: 1 — Project Scaffolding
**Parallel**: Yes — can run alongside T003

Create the Liquibase root file and the first two changesets:
- `db.changelog-master.xml`: declares `include` references to all individual changesets in order
- `001-create-users.xml`: creates `users` table with columns: `id` (BIGSERIAL PK), `email` (VARCHAR UNIQUE NOT NULL), `password_hash` (VARCHAR NOT NULL), `display_name` (VARCHAR), `created_at` (TIMESTAMP NOT NULL)

**Output files**:
- `finance-tracker/src/main/resources/db/changelog/db.changelog-master.xml`
- `finance-tracker/src/main/resources/db/changelog/001-create-users.xml`

---

### T005 — Create Liquibase changesets for categories, default seed, and transactions [P]

**Phase**: 1 — Project Scaffolding
**Parallel**: Yes — can run alongside T006 (separate files)

Create the following Liquibase changesets:
- `002-create-categories.xml`: creates `categories` table with columns: `id` (BIGSERIAL PK), `name` (VARCHAR NOT NULL), `is_system_default` (BOOLEAN NOT NULL DEFAULT FALSE), `user_id` (BIGINT FK to `users`, nullable)
- `003-seed-default-categories.xml`: inserts nine rows with `is_system_default = true` and `user_id = NULL`: Food, Transport, Utilities, Salary, Entertainment, Healthcare, Housing, Education, Other
- `004-create-transactions.xml`: creates `transactions` table with columns: `id` (BIGSERIAL PK), `user_id` (BIGINT NOT NULL FK), `amount` (DECIMAL(19,4) NOT NULL), `type` (VARCHAR NOT NULL), `category_id` (BIGINT NOT NULL FK), `date` (DATE NOT NULL), `description` (VARCHAR(500)), `created_at` (TIMESTAMP NOT NULL), `updated_at` (TIMESTAMP)

**Output files**:
- `finance-tracker/src/main/resources/db/changelog/002-create-categories.xml`
- `finance-tracker/src/main/resources/db/changelog/003-seed-default-categories.xml`
- `finance-tracker/src/main/resources/db/changelog/004-create-transactions.xml`

---

### T006 — Create Liquibase changesets for budgets and goals [P]

**Phase**: 1 — Project Scaffolding
**Parallel**: Yes — can run alongside T005 (separate files)

Create the following Liquibase changesets:
- `005-create-budgets.xml`: creates `budgets` table with columns: `id` (BIGSERIAL PK), `user_id` (BIGINT NOT NULL FK), `category_id` (BIGINT NOT NULL FK), `limit_amount` (DECIMAL(19,4) NOT NULL), `month` (INT NOT NULL), `year` (INT NOT NULL), `version` (BIGINT NOT NULL DEFAULT 0); unique constraint on `(user_id, category_id, month, year)`
- `006-create-goals.xml`: creates `goals` table with columns: `id` (BIGSERIAL PK), `user_id` (BIGINT NOT NULL FK), `name` (VARCHAR(200) NOT NULL), `target_amount` (DECIMAL(19,4) NOT NULL), `contributed_amount` (DECIMAL(19,4) NOT NULL DEFAULT 0), `target_date` (DATE NOT NULL), `status` (VARCHAR NOT NULL DEFAULT 'IN_PROGRESS'), `created_at` (TIMESTAMP NOT NULL)

**Output files**:
- `finance-tracker/src/main/resources/db/changelog/005-create-budgets.xml`
- `finance-tracker/src/main/resources/db/changelog/006-create-goals.xml`

---

### T007 — Create Checkstyle configuration [P]

**Phase**: 1 — Project Scaffolding
**Parallel**: Yes — independent file, can run alongside T008

Create `config/checkstyle.xml` with rules that:
- Flag use of `double` or `float` in classes within the `model` and `dto` packages
- Enforce method length limit of 30 lines
- Enforce class length limit of 300 lines
- Follow standard Google or Sun Java style rules as the base

**Output file**: `finance-tracker/config/checkstyle.xml`

---

### T008 — Implement exception infrastructure: ErrorResponse record and GlobalExceptionHandler [P]

**Phase**: 1 — Project Scaffolding
**Parallel**: Yes — can run alongside T007; independent of auth/entity code

Implement the two exception infrastructure classes:

1. `ErrorResponse.java`: Java 17 record with fields `timestamp`, `status`, `error`, `message`, `path`, and optional `fieldErrors` (list of `FieldError` records with `field` and `message`). Annotate fields with `@Schema` for OpenAPI.
2. `GlobalExceptionHandler.java`: `@ControllerAdvice` class handling:
   - `MethodArgumentNotValidException` → `400 Bad Request` with `fieldErrors` array populated
   - `ResourceNotFoundException` → `404 Not Found`
   - `ForbiddenOperationException` → `403 Forbidden`
   - `DuplicateResourceException` → `409 Conflict`
   - `ReferentialIntegrityException` → `409 Conflict`
   - `OptimisticLockException` / `ObjectOptimisticLockingFailureException` → `409 Conflict`
   - Catch-all `Exception` → `500 Internal Server Error`

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/exception/ErrorResponse.java`
- `finance-tracker/src/main/java/com/financetracker/exception/GlobalExceptionHandler.java`
- `finance-tracker/src/main/java/com/financetracker/exception/ResourceNotFoundException.java`
- `finance-tracker/src/main/java/com/financetracker/exception/ForbiddenOperationException.java`
- `finance-tracker/src/main/java/com/financetracker/exception/DuplicateResourceException.java`
- `finance-tracker/src/main/java/com/financetracker/exception/ReferentialIntegrityException.java`

---

### T009 — Implement PaginationConfig: enforce page size limits [P]

**Phase**: 1 — Project Scaffolding
**Parallel**: Yes — can run alongside T007, T008

Implement pagination enforcement so that requests with `size > 25` receive `400 Bad Request`.
Options:
- Implement a `WebMvcConfigurer` that customizes `PageableHandlerMethodArgumentResolver` with `setMaxPageSize(25)` and `setFallbackPageable(PageRequest.of(0, 20))`
- OR implement a `@ControllerAdvice` that intercepts and validates `Pageable` arguments before they reach the service layer

**Output file**: `finance-tracker/src/main/java/com/financetracker/config/PaginationConfig.java`

---

### T010 — Implement OpenApiConfig [P]

**Phase**: 1 — Project Scaffolding
**Parallel**: Yes — can run alongside T007, T008, T009

Create `OpenApiConfig.java` that configures:
- API title: "Finance Tracker API"
- API version: "v1"
- JWT bearer authentication scheme (so Swagger UI shows the Authorize button)
- Server URL for dev and prod

**Output file**: `finance-tracker/src/main/java/com/financetracker/config/OpenApiConfig.java`

---

## Category: Tests — Architecture and Security (TDD First)

> These tests must be written before implementation of their target components.
> They will initially fail (red). Implementation tasks make them pass (green).

---

### T011 — Write ArchUnit architecture test suite

**Phase**: 1 — Project Scaffolding (Tests)
**Parallel**: No (depends on T001 project structure being in place)

Write `ArchitectureTest.java` using ArchUnit asserting:
- Controllers do not depend on repository classes directly
- Services do not depend on any HTTP types (classes in `jakarta.servlet` or `org.springframework.web.servlet`)
- Repositories are only called from the service layer
- All `@RestController` annotated classes reside in the `controller` package
- All `@Service` annotated classes reside in the `service` package
- All `JpaRepository` extending interfaces reside in the `repository` package
- All `@Entity` annotated classes reside in the `model` package

**Output file**: `finance-tracker/src/test/java/com/financetracker/architecture/ArchitectureTest.java`

---

## Category: Core — Security and Authentication (Phase 2)

> Prerequisites: T001–T011 complete. Phase 1 gate must pass before starting Phase 2.

---

### T012 — Implement User JPA entity and UserRepository [P]

**Phase**: 2 — Security and Authentication
**Parallel**: Yes — can run alongside T013

Implement the `User` entity and its repository:
1. `User.java`: JPA entity with fields `id` (Long, auto-generated), `email` (String, unique, not null), `passwordHash` (String, not null, column name `password_hash`), `displayName` (String, nullable), `createdAt` (LocalDateTime, not null). Use Java 17 conventions. No `double` or `float` fields. Implement `UserDetails` interface for Spring Security compatibility or create a separate adapter.
2. `UserRepository.java`: extends `JpaRepository<User, Long>` with `Optional<User> findByEmail(String email)`

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/model/User.java`
- `finance-tracker/src/main/java/com/financetracker/repository/UserRepository.java`

---

### T013 — Implement JwtService [P]

**Phase**: 2 — Security and Authentication
**Parallel**: Yes — can run alongside T012

Implement `JwtService.java` in the `security` package using the `com.auth0:java-jwt` library:
- `generateToken(UserDetails userDetails)`: creates a signed JWT using `Algorithm.HMAC256(secret)`, with subject set to the username, issued-at set to now, and expiry set to `now + expiryMs` (read from `${app.jwt.expiry-ms}`, default `7200000`)
- `validateToken(String token, UserDetails userDetails)`: decodes and verifies the token using `JWT.require(algorithm).build().verify(token)`, returns `true` if the subject matches `userDetails.getUsername()` and the token is not expired
- JWT secret read from `${app.jwt.secret}` via `@Value`

**Output file**: `finance-tracker/src/main/java/com/financetracker/security/JwtService.java`

---

### T014 — Implement JwtAuthenticationFilter and UserDetailsServiceImpl [P]

**Phase**: 2 — Security and Authentication
**Parallel**: Yes — can run alongside T012, T013 (depends on T013 for JwtService)

Implement:
1. `JwtAuthenticationFilter.java`: extends `OncePerRequestFilter`. Extracts the `Authorization: Bearer <token>` header, calls `JwtService.validateToken`, and if valid, creates a `UsernamePasswordAuthenticationToken` and sets it on the `SecurityContextHolder`.
2. `UserDetailsServiceImpl.java`: implements `UserDetailsService`. Loads `User` from `UserRepository` by email; throws `UsernameNotFoundException` if not found.

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/security/JwtAuthenticationFilter.java`
- `finance-tracker/src/main/java/com/financetracker/security/UserDetailsServiceImpl.java`

---

### T015 — Implement SecurityConfig and CorsConfig

**Phase**: 2 — Security and Authentication
**Parallel**: No (depends on T013, T014)

Implement:
1. `SecurityConfig.java`: `@Configuration`, `@EnableWebSecurity`. Disables session creation (stateless). Permits `/api/v1/auth/**` without authentication. Requires authentication on all other paths. Registers `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`. Declares `BCryptPasswordEncoder` bean. Declares `AuthenticationManager` bean.
2. `CorsConfig.java`: reads allowed origins from `${app.cors.allowed-origins}`. Configures allowed methods (GET, POST, PUT, PATCH, DELETE, OPTIONS). No wildcard origins.

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/config/SecurityConfig.java`
- `finance-tracker/src/main/java/com/financetracker/config/CorsConfig.java`

---

### T016 — Implement Auth DTOs [P]

**Phase**: 2 — Security and Authentication
**Parallel**: Yes — can run alongside T015 (pure data classes, no logic)

Implement the following Java 17 records with Jakarta Bean Validation annotations:
- `RegisterRequest.java`: fields `email` (`@NotBlank`, `@Email`), `password` (`@NotBlank`, `@Size(min=8)`), `displayName` (optional String)
- `LoginRequest.java`: fields `email` (`@NotBlank`, `@Email`), `password` (`@NotBlank`)
- `TokenResponse.java`: fields `token` (String), `expiresIn` (long, milliseconds)

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/dto/auth/RegisterRequest.java`
- `finance-tracker/src/main/java/com/financetracker/dto/auth/LoginRequest.java`
- `finance-tracker/src/main/java/com/financetracker/dto/auth/TokenResponse.java`

---

### T017 — Implement User DTOs [P]

**Phase**: 2 — Security and Authentication
**Parallel**: Yes — can run alongside T016 (separate package)

Implement the following DTO classes:
- `UserProfileResponse.java`: Java 17 record with fields `id`, `email`, `displayName`, `createdAt`
- `UpdateProfileRequest.java`: Java 17 record (or class) with `displayName` (`@NotBlank`)
- `ChangePasswordRequest.java`: Java 17 record (or class) with `currentPassword` (`@NotBlank`), `newPassword` (`@NotBlank`, `@Size(min=8)`)

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/dto/user/UserProfileResponse.java`
- `finance-tracker/src/main/java/com/financetracker/dto/user/UpdateProfileRequest.java`
- `finance-tracker/src/main/java/com/financetracker/dto/user/ChangePasswordRequest.java`

---

### T018 — Implement UserMapper [P]

**Phase**: 2 — Security and Authentication
**Parallel**: Yes — can run alongside T016, T017 (depends on T012 User entity and T017 UserProfileResponse)

Implement `UserMapper.java` as a MapStruct `@Mapper` interface that maps `User` entity to `UserProfileResponse` record.

**Output file**: `finance-tracker/src/main/java/com/financetracker/mapper/UserMapper.java`

---

### T019 — Implement AuthService

**Phase**: 2 — Security and Authentication
**Parallel**: No (depends on T012, T013, T015, T016)

Implement `AuthService.java` with `@Service`:
- `register(RegisterRequest request)`: checks email uniqueness via `UserRepository.findByEmail`; throws `DuplicateResourceException` if taken; hashes password with `BCryptPasswordEncoder`; persists `User`; returns a success message or `UserProfileResponse`
- `login(LoginRequest request)`: loads user by email; uses `AuthenticationManager.authenticate(UsernamePasswordAuthenticationToken)`; if successful, generates JWT via `JwtService.generateToken`; returns `TokenResponse`
All methods `@Transactional` on write paths.

**Output file**: `finance-tracker/src/main/java/com/financetracker/service/AuthService.java`

---

### T020 — Implement UserService

**Phase**: 2 — Security and Authentication
**Parallel**: No (depends on T012, T017, T018)

Implement `UserService.java` with `@Service`:
- `getProfile(Long userId)`: loads `User` by id; throws `ResourceNotFoundException` if absent; returns `UserProfileResponse` via `UserMapper`
- `updateDisplayName(Long userId, UpdateProfileRequest request)`: updates `displayName`; returns updated `UserProfileResponse`
- `changePassword(Long userId, ChangePasswordRequest request)`: verifies `currentPassword` matches stored hash via `BCryptPasswordEncoder.matches`; hashes and stores `newPassword`; returns `204` (void)
- `deleteAccount(Long userId)`: deletes user and all owned data (implement explicit deletion order or rely on `CascadeType.ALL` + `orphanRemoval`); returns `204` (void)
All write methods annotated `@Transactional`.

**Output file**: `finance-tracker/src/main/java/com/financetracker/service/UserService.java`

---

### T021 — Implement AuthController

**Phase**: 2 — Security and Authentication
**Parallel**: No (depends on T019, T016)

Implement `AuthController.java` with `@RestController`, `@RequestMapping("/api/v1/auth")`:
- `POST /api/v1/auth/register`: calls `AuthService.register`; returns `201 Created`
- `POST /api/v1/auth/login`: calls `AuthService.login`; returns `200 OK` with `TokenResponse`
Annotate all endpoints with `@Operation`, `@ApiResponse`, `@Schema` for SpringDoc.

**Output file**: `finance-tracker/src/main/java/com/financetracker/controller/AuthController.java`

---

### T022 — Implement UserController

**Phase**: 2 — Security and Authentication
**Parallel**: No (depends on T020, T017)

Implement `UserController.java` with `@RestController`, `@RequestMapping("/api/v1/users")`:
- `GET /api/v1/users/me`: returns `UserProfileResponse` (`200 OK`)
- `PUT /api/v1/users/me`: updates display name; returns updated `UserProfileResponse` (`200 OK`)
- `PUT /api/v1/users/me/password`: changes password; returns `204 No Content`
- `DELETE /api/v1/users/me`: deletes account; returns `204 No Content`
Extract the authenticated user's ID from the Spring Security `Authentication` principal. Annotate all endpoints with SpringDoc annotations.

**Output file**: `finance-tracker/src/main/java/com/financetracker/controller/UserController.java`

---

## Category: Tests — Security Integration (TDD)

---

### T023 — Write UserRepository @DataJpaTest [P]

**Phase**: 2 — Security and Authentication (Tests)
**Parallel**: Yes — can run alongside T024, T025 (separate test file)

Write `@DataJpaTest` tests for `UserRepository`:
- `findByEmail` returns the correct user when the email exists
- `findByEmail` returns `Optional.empty()` when the email does not exist
- Saving a user with a duplicate email throws `DataIntegrityViolationException`
- `BigDecimal` round-trip: no monetary fields in User, but verify `createdAt` persistence

**Output file**: `finance-tracker/src/test/java/com/financetracker/repository/UserRepositoryTest.java`

---

### T024 — Write AuthService unit tests [P]

**Phase**: 2 — Security and Authentication (Tests)
**Parallel**: Yes — can run alongside T023, T025

Write JUnit 5 + Mockito unit tests for `AuthService`:
- `register`: success path creates user with hashed password
- `register`: throws `DuplicateResourceException` when email already exists
- `login`: success path returns `TokenResponse` with a non-null token
- `login`: throws appropriate exception when credentials are invalid
Mock `UserRepository`, `BCryptPasswordEncoder`, `AuthenticationManager`, and `JwtService`.

**Output file**: `finance-tracker/src/test/java/com/financetracker/service/AuthServiceTest.java`

---

### T025 — Write UserService unit tests [P]

**Phase**: 2 — Security and Authentication (Tests)
**Parallel**: Yes — can run alongside T023, T024

Write JUnit 5 + Mockito unit tests for `UserService`:
- `getProfile`: returns `UserProfileResponse` for a valid user id
- `getProfile`: throws `ResourceNotFoundException` when user not found
- `updateDisplayName`: updates and returns updated profile
- `changePassword`: succeeds when `currentPassword` matches; throws exception when it does not match
- `deleteAccount`: calls repository delete
Mock `UserRepository`, `BCryptPasswordEncoder`, `UserMapper`.

**Output file**: `finance-tracker/src/test/java/com/financetracker/service/UserServiceTest.java`

---

### T026 — Write AuthController and UserController integration tests [P]

**Phase**: 2 — Security and Authentication (Tests)
**Parallel**: Yes — can run alongside T023, T024, T025

Write `@SpringBootTest` + `MockMvc` integration tests:
- `AuthControllerTest`: POST register with valid body → 201; POST register with duplicate email → 409; POST register with missing fields → 400 with `fieldErrors`; POST login with valid credentials → 200 with `TokenResponse`; POST login with wrong password → 401
- `UserControllerTest`: GET /me with valid JWT → 200; GET /me without JWT → 401; PUT /me → 200; PUT /me/password with wrong current password → appropriate error; DELETE /me → 204

**Output files**:
- `finance-tracker/src/test/java/com/financetracker/controller/AuthControllerTest.java`
- `finance-tracker/src/test/java/com/financetracker/controller/UserControllerTest.java`

---

### T027 — Write SecurityIntegrationTest

**Phase**: 2 — Security and Authentication (Tests)
**Parallel**: No (depends on all controllers being registered — best written after T021, T022)

Write `SecurityIntegrationTest.java` using `@SpringBootTest` + `MockMvc`:
- Send an unauthenticated request to each protected endpoint and assert `401 Unauthorized` for all
- Send a request to `/api/v1/auth/register` without JWT and assert it is reachable (not 401)
- Send a request to `/api/v1/auth/login` without JWT and assert it is reachable (not 401)
- Assert that all error responses conform to `{ timestamp, status, error, message, path }` schema

**Output file**: `finance-tracker/src/test/java/com/financetracker/security/SecurityIntegrationTest.java`

---

## Category: Core — Category Management (Phase 3)

> Prerequisites: Phase 2 gate must pass (T012–T027 complete).

---

### T028 — Implement Category JPA entity [P]

**Phase**: 3 — Category Management
**Parallel**: Yes — can run alongside T029

Implement `Category.java`: JPA entity with fields `id` (Long, auto-generated), `name` (String, not null), `isSystemDefault` (boolean), `user` (nullable ManyToOne to `User`). No `double` or `float` fields.

**Output file**: `finance-tracker/src/main/java/com/financetracker/model/Category.java`

---

### T029 — Implement CategoryRepository [P]

**Phase**: 3 — Category Management
**Parallel**: Yes — can run alongside T028

Implement `CategoryRepository.java` extending `JpaRepository<Category, Long>` with:
- `List<Category> findByUserIsNullOrUserId(Long userId)`: returns system defaults and user-owned categories
- `boolean existsByNameAndUserId(String name, Long userId)`: duplicate name check
- `boolean existsByIdAndUserIsNull(Long id)`: identifies system-default categories
- A `@Query` method (JPQL) to count how many transactions and/or budgets reference a given category id (for referential integrity check before delete)

**Output file**: `finance-tracker/src/main/java/com/financetracker/repository/CategoryRepository.java`

---

### T030 — Implement Category DTOs and CategoryMapper [P]

**Phase**: 3 — Category Management
**Parallel**: Yes — can run alongside T028, T029

Implement:
- `CategoryRequest.java`: Java 17 record (or class) with `name` (`@NotBlank`, `@Size(max=100)`)
- `CategoryResponse.java`: Java 17 record with fields `id`, `name`, `isSystemDefault`
- `CategoryMapper.java`: MapStruct `@Mapper` interface mapping `Category` entity to `CategoryResponse`

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/dto/category/CategoryRequest.java`
- `finance-tracker/src/main/java/com/financetracker/dto/category/CategoryResponse.java`
- `finance-tracker/src/main/java/com/financetracker/mapper/CategoryMapper.java`

---

### T031 — Implement CategoryService

**Phase**: 3 — Category Management
**Parallel**: No (depends on T028, T029, T030)

Implement `CategoryService.java` with `@Service`:
- `getCategories(Long userId)`: calls `CategoryRepository.findByUserIsNullOrUserId(userId)`; maps to `List<CategoryResponse>` via `CategoryMapper`
- `createCategory(Long userId, CategoryRequest request)`: checks `existsByNameAndUserId`; throws `DuplicateResourceException` if duplicate; persists; returns `CategoryResponse`
- `deleteCategory(Long userId, Long categoryId)`: loads category; throws `ResourceNotFoundException` if absent; throws `ForbiddenOperationException` if `isSystemDefault`; checks referential integrity (transactions or budgets reference category); throws `ReferentialIntegrityException` if referenced; otherwise deletes
Write operations annotated `@Transactional`.

**Output file**: `finance-tracker/src/main/java/com/financetracker/service/CategoryService.java`

---

### T032 — Implement CategoryController

**Phase**: 3 — Category Management
**Parallel**: No (depends on T031)

Implement `CategoryController.java` with `@RestController`, `@RequestMapping("/api/v1/categories")`:
- `GET /api/v1/categories` → `200 OK` with `List<CategoryResponse>`
- `POST /api/v1/categories` → `201 Created` with created `CategoryResponse`
- `DELETE /api/v1/categories/{id}` → `204 No Content`
Annotate all endpoints with `@Operation`, `@ApiResponse`, `@Schema`.

**Output file**: `finance-tracker/src/main/java/com/financetracker/controller/CategoryController.java`

---

## Category: Tests — Category (TDD)

---

### T033 — Write CategoryRepository @DataJpaTest [P]

**Phase**: 3 — Category Management (Tests)
**Parallel**: Yes — can run alongside T034, T035

Write `@DataJpaTest` tests for `CategoryRepository`:
- `findByUserIsNullOrUserId` returns system defaults when user has no custom categories
- `findByUserIsNullOrUserId` returns system defaults and user's categories combined
- `existsByNameAndUserId` returns true for duplicate, false for unique name
- Referential integrity query returns correct count when transactions or budgets reference a category

**Output file**: `finance-tracker/src/test/java/com/financetracker/repository/CategoryRepositoryTest.java`

---

### T034 — Write CategoryService unit tests [P]

**Phase**: 3 — Category Management (Tests)
**Parallel**: Yes — can run alongside T033, T035

Write JUnit 5 + Mockito unit tests for `CategoryService`:
- `getCategories`: returns merged list of system defaults and user categories
- `createCategory`: success path persists and returns `CategoryResponse`
- `createCategory`: throws `DuplicateResourceException` on duplicate name
- `deleteCategory`: throws `ForbiddenOperationException` for system-default category
- `deleteCategory`: throws `ReferentialIntegrityException` when category is referenced
- `deleteCategory`: success path deletes unreferenced custom category
Mock `CategoryRepository`, `CategoryMapper`.

**Output file**: `finance-tracker/src/test/java/com/financetracker/service/CategoryServiceTest.java`

---

### T035 — Write CategoryController integration tests [P]

**Phase**: 3 — Category Management (Tests)
**Parallel**: Yes — can run alongside T033, T034

Write `@SpringBootTest` + `MockMvc` integration tests for `CategoryController`:
- GET /categories returns list including nine seeded system defaults
- POST /categories with unique name → 201 with body
- POST /categories with duplicate name → 409
- DELETE /categories/{id} for a user-owned category → 204
- DELETE /categories/{id} for a system-default category → 403
- DELETE /categories/{id} for a category with referenced transactions → 409
- All error responses conform to `{ timestamp, status, error, message, path }` schema

**Output file**: `finance-tracker/src/test/java/com/financetracker/controller/CategoryControllerTest.java`

---

## Category: Core — Transaction Management (Phase 4)

> Prerequisites: Phase 3 gate must pass (T028–T035 complete).

---

### T036 — Implement Transaction entity and enums [P]

**Phase**: 4 — Transaction Management
**Parallel**: Yes — can run alongside T037

Implement:
- `TransactionType.java`: enum with values `INCOME`, `EXPENSE`
- `Transaction.java`: JPA entity. `amount` annotated with `@Column(precision=19, scale=4)`. FK ManyToOne to `User` and `Category`. `date` is `LocalDate`. `description` optional, max 500 chars. `createdAt` set on creation. `updatedAt` updated via `@PreUpdate`. No `double` or `float` fields.

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/model/TransactionType.java`
- `finance-tracker/src/main/java/com/financetracker/model/Transaction.java`

---

### T037 — Implement TransactionRepository [P]

**Phase**: 4 — Transaction Management
**Parallel**: Yes — can run alongside T036

Implement `TransactionRepository.java` extending `JpaRepository<Transaction, Long>` with:
- `Page<Transaction> findByUserIdOrderByDateDesc(Long userId, Pageable pageable)`
- A filter-capable query method: use Spring Data JPA `Specification<Transaction>` or a `@Query` that supports optional filter parameters `type`, `categoryId`, `startDate`, `endDate` (all combinable, all optional)

**Output file**: `finance-tracker/src/main/java/com/financetracker/repository/TransactionRepository.java`

---

### T038 — Implement Transaction DTOs and TransactionMapper [P]

**Phase**: 4 — Transaction Management
**Parallel**: Yes — can run alongside T036, T037

Implement:
- `TransactionRequest.java`: fields `amount` (`@NotNull`, `@DecimalMin("0.01")`), `type` (`@NotNull`), `categoryId` (`@NotNull`), `date` (`@NotNull`), `description` (optional, `@Size(max=500)`)
- `TransactionResponse.java`: Java 17 record with fields `id`, `amount`, `type`, `categoryId`, `categoryName`, `date`, `description`, `createdAt`, `updatedAt`
- `TransactionFilterParams.java`: contains optional fields `type`, `categoryId`, `startDate`, `endDate`
- `TransactionMapper.java`: MapStruct `@Mapper` interface mapping `Transaction` to `TransactionResponse`

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/dto/transaction/TransactionRequest.java`
- `finance-tracker/src/main/java/com/financetracker/dto/transaction/TransactionResponse.java`
- `finance-tracker/src/main/java/com/financetracker/dto/transaction/TransactionFilterParams.java`
- `finance-tracker/src/main/java/com/financetracker/mapper/TransactionMapper.java`

---

### T039 — Implement TransactionService

**Phase**: 4 — Transaction Management
**Parallel**: No (depends on T036, T037, T038)

Implement `TransactionService.java` with `@Service`:
- `createTransaction(Long userId, TransactionRequest request)`: validates that the category belongs to the user or is a system default; persists; returns `TransactionResponse`. Annotated `@Transactional`.
- `getTransactions(Long userId, TransactionFilterParams filters, Pageable pageable)`: returns paginated, filtered `Page<TransactionResponse>`
- `getTransaction(Long userId, Long id)`: loads transaction; throws `ResourceNotFoundException` if absent; throws `ForbiddenOperationException` if `transaction.userId != userId`; returns `TransactionResponse`
- `updateTransaction(Long userId, Long id, TransactionRequest request)`: ownership check; updates fields; returns `TransactionResponse`. Annotated `@Transactional`.
- `deleteTransaction(Long userId, Long id)`: ownership check; deletes. Annotated `@Transactional`.

**Output file**: `finance-tracker/src/main/java/com/financetracker/service/TransactionService.java`

---

### T040 — Implement TransactionController

**Phase**: 4 — Transaction Management
**Parallel**: No (depends on T039)

Implement `TransactionController.java` with `@RestController`, `@RequestMapping("/api/v1/transactions")`:
- `GET /api/v1/transactions` → `200 OK` paginated (accepts `Pageable` and `TransactionFilterParams` as query params)
- `POST /api/v1/transactions` → `201 Created`
- `GET /api/v1/transactions/{id}` → `200 OK`
- `PUT /api/v1/transactions/{id}` → `200 OK`
- `DELETE /api/v1/transactions/{id}` → `204 No Content`
Annotate all endpoints with `@Operation`, `@ApiResponse`, `@Schema`.

**Output file**: `finance-tracker/src/main/java/com/financetracker/controller/TransactionController.java`

---

## Category: Tests — Transaction (TDD)

---

### T041 — Write TransactionRepository @DataJpaTest [P]

**Phase**: 4 — Transaction Management (Tests)
**Parallel**: Yes — can run alongside T042, T043

Write `@DataJpaTest` tests for `TransactionRepository`:
- `findByUserIdOrderByDateDesc` returns only the requesting user's transactions in descending date order
- Filter by `type` returns only transactions matching that type
- Filter by `categoryId` returns only transactions for that category
- Filter by `startDate`/`endDate` date range returns only in-range transactions
- Combined filters work correctly
- `BigDecimal` round-trip: store and retrieve an amount with 4 decimal places; assert `compareTo` equality

**Output file**: `finance-tracker/src/test/java/com/financetracker/repository/TransactionRepositoryTest.java`

---

### T042 — Write TransactionService unit tests [P]

**Phase**: 4 — Transaction Management (Tests)
**Parallel**: Yes — can run alongside T041, T043

Write JUnit 5 + Mockito unit tests for `TransactionService`:
- `createTransaction`: success path with user-owned category
- `createTransaction`: success path with system-default category
- `createTransaction`: future-dated transaction is accepted
- `createTransaction`: negative amount causes `400 Bad Request` (validation; test at DTO level or service defensive check)
- `getTransaction`: returns correct response for own transaction
- `getTransaction`: throws `ForbiddenOperationException` for another user's transaction
- `updateTransaction`: updates and returns updated response
- `deleteTransaction`: calls repository delete
Mock `TransactionRepository`, `CategoryRepository`, `TransactionMapper`.

**Output file**: `finance-tracker/src/test/java/com/financetracker/service/TransactionServiceTest.java`

---

### T043 — Write TransactionController integration tests [P]

**Phase**: 4 — Transaction Management (Tests)
**Parallel**: Yes — can run alongside T041, T042

Write `@SpringBootTest` + `MockMvc` integration tests for `TransactionController`:
- POST with valid body → 201
- POST with missing `amount` → 400 with `fieldErrors`
- POST with negative amount → 400
- GET list → 200 with pagination defaults (`page=0`, `size=20`)
- GET list with `size=26` → 400 (max page size exceeded)
- GET list with filter `type=EXPENSE` → returns only EXPENSE transactions
- GET `/{id}` for own transaction → 200
- GET `/{id}` for another user's transaction → 403
- PUT `/{id}` with valid body → 200
- DELETE `/{id}` → 204
- All 401 paths when no JWT provided

**Output file**: `finance-tracker/src/test/java/com/financetracker/controller/TransactionControllerTest.java`

---

## Category: Core — Budget Management (Phase 5)

> Prerequisites: Phase 4 gate must pass (T036–T043 complete).

---

### T044 — Implement Budget JPA entity [P]

**Phase**: 5 — Budget and Goal Management
**Parallel**: Yes — can run alongside T047 (Goal entity)

Implement `Budget.java`: JPA entity. `limitAmount` annotated with `@Column(precision=19, scale=4)`. FK ManyToOne to `User` and `Category`. `month` (int, 1–12), `year` (int). `@Version` field (Long) for optimistic locking. `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "category_id", "month", "year"}))`. No `double` or `float` fields.

**Output file**: `finance-tracker/src/main/java/com/financetracker/model/Budget.java`

---

### T045 — Implement BudgetRepository [P]

**Phase**: 5 — Budget and Goal Management
**Parallel**: Yes — can run alongside T044, T047, T048

Implement `BudgetRepository.java` extending `JpaRepository<Budget, Long>` with:
- `List<Budget> findByUserId(Long userId)`
- `boolean existsByUserIdAndCategoryIdAndMonthAndYear(Long userId, Long categoryId, int month, int year)`
- A `@Query` JPQL method `findByUserIdWithSpentAmount(Long userId)` that joins the `transactions` table and computes the `SUM` of EXPENSE amounts per budget period, returning a list of projections or tuples containing budget data + computed `spentAmount`

**Output file**: `finance-tracker/src/main/java/com/financetracker/repository/BudgetRepository.java`

---

### T046 — Implement Budget DTOs and BudgetMapper [P]

**Phase**: 5 — Budget and Goal Management
**Parallel**: Yes — can run alongside T044, T045

Implement:
- `BudgetRequest.java`: fields `categoryId` (`@NotNull`), `limitAmount` (`@NotNull`, `@DecimalMin("0.01")`), `month` (`@NotNull`, `@Min(1)`, `@Max(12)`), `year` (`@NotNull`)
- `BudgetResponse.java`: Java 17 record with fields `id`, `categoryId`, `categoryName`, `limitAmount`, `month`, `year`, `spentAmount`, `overLimit` (boolean)
- `BudgetMapper.java`: MapStruct `@Mapper` interface mapping `Budget` entity + computed `spentAmount` to `BudgetResponse` record

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/dto/budget/BudgetRequest.java`
- `finance-tracker/src/main/java/com/financetracker/dto/budget/BudgetResponse.java`
- `finance-tracker/src/main/java/com/financetracker/mapper/BudgetMapper.java`

---

### T047 — Implement Goal JPA entity and GoalStatus enum [P]

**Phase**: 5 — Budget and Goal Management
**Parallel**: Yes — can run alongside T044 (separate entity)

Implement:
- `GoalStatus.java`: enum with values `IN_PROGRESS`, `ACHIEVED`
- `Goal.java`: JPA entity. `targetAmount` and `contributedAmount` both annotated with `@Column(precision=19, scale=4)`. `contributedAmount` defaults to `BigDecimal.ZERO`. `GoalStatus` stored as `@Enumerated(EnumType.STRING)`. `targetDate` is `LocalDate`. `createdAt` set on creation. No `double` or `float` fields.

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/model/GoalStatus.java`
- `finance-tracker/src/main/java/com/financetracker/model/Goal.java`

---

### T048 — Implement GoalRepository [P]

**Phase**: 5 — Budget and Goal Management
**Parallel**: Yes — can run alongside T047

Implement `GoalRepository.java` extending `JpaRepository<Goal, Long>` with:
- `List<Goal> findByUserId(Long userId)`

**Output file**: `finance-tracker/src/main/java/com/financetracker/repository/GoalRepository.java`

---

### T049 — Implement Goal DTOs and GoalMapper [P]

**Phase**: 5 — Budget and Goal Management
**Parallel**: Yes — can run alongside T047, T048

Implement:
- `GoalRequest.java`: fields `name` (`@NotBlank`, `@Size(max=200)`), `targetAmount` (`@NotNull`, `@DecimalMin("0.01")`), `targetDate` (`@NotNull`)
- `GoalResponse.java`: Java 17 record with fields `id`, `name`, `targetAmount`, `contributedAmount`, `remainingAmount`, `progressPercentage` (BigDecimal, 2 decimal places), `targetDate`, `status`, `createdAt`
- `ContributionRequest.java`: fields `amount` (`@NotNull`, `@DecimalMin("0.01")`), `note` (optional String)
- `GoalMapper.java`: MapStruct `@Mapper` interface mapping `Goal` entity to `GoalResponse` (computed fields `remainingAmount` and `progressPercentage` may need a custom mapping method or be computed in the service and passed in)

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/dto/goal/GoalRequest.java`
- `finance-tracker/src/main/java/com/financetracker/dto/goal/GoalResponse.java`
- `finance-tracker/src/main/java/com/financetracker/dto/goal/ContributionRequest.java`
- `finance-tracker/src/main/java/com/financetracker/mapper/GoalMapper.java`

---

### T050 — Implement BudgetService

**Phase**: 5 — Budget and Goal Management
**Parallel**: No (depends on T044, T045, T046)

Implement `BudgetService.java` with `@Service`:
- `createBudget(Long userId, BudgetRequest request)`: checks uniqueness via `existsByUserIdAndCategoryIdAndMonthAndYear`; throws `DuplicateResourceException` on conflict; persists; returns `BudgetResponse`. Annotated `@Transactional`.
- `getBudgets(Long userId)`: calls `findByUserIdWithSpentAmount`; computes `overLimit = spentAmount > limitAmount`; returns `List<BudgetResponse>`
- `getBudget(Long userId, Long id)`: loads with computed amounts; throws `ResourceNotFoundException` if absent; ownership check; returns `BudgetResponse`
- `updateBudget(Long userId, Long id, BudgetRequest request)`: ownership check; updates `limitAmount`; returns updated `BudgetResponse`. Annotated `@Transactional`.
- `deleteBudget(Long userId, Long id)`: ownership check; deletes. Annotated `@Transactional`.
All `BigDecimal` arithmetic uses `HALF_UP` rounding mode.

**Output file**: `finance-tracker/src/main/java/com/financetracker/service/BudgetService.java`

---

### T051 — Implement BudgetController

**Phase**: 5 — Budget and Goal Management
**Parallel**: No (depends on T050)

Implement `BudgetController.java` with `@RestController`, `@RequestMapping("/api/v1/budgets")`:
- `GET /api/v1/budgets` → `200 OK`
- `POST /api/v1/budgets` → `201 Created`
- `GET /api/v1/budgets/{id}` → `200 OK`
- `PUT /api/v1/budgets/{id}` → `200 OK`
- `DELETE /api/v1/budgets/{id}` → `204 No Content`
Annotate all endpoints with SpringDoc annotations.

**Output file**: `finance-tracker/src/main/java/com/financetracker/controller/BudgetController.java`

---

### T052 — Implement GoalService

**Phase**: 5 — Budget and Goal Management
**Parallel**: No (depends on T047, T048, T049)

Implement `GoalService.java` with `@Service`:
- `createGoal(Long userId, GoalRequest request)`: persists new goal with status `IN_PROGRESS` and `contributedAmount = BigDecimal.ZERO`; returns `GoalResponse`. Annotated `@Transactional`.
- `getGoals(Long userId)`: returns `List<GoalResponse>` with computed fields
- `getGoal(Long userId, Long id)`: ownership check; returns `GoalResponse` with computed fields
- `updateGoal(Long userId, Long id, GoalRequest request)`: ownership check; updates `name`, `targetAmount`, `targetDate`; returns updated `GoalResponse`. Annotated `@Transactional`.
- `deleteGoal(Long userId, Long id)`: ownership check; deletes. Annotated `@Transactional`.
- `addContribution(Long userId, Long id, ContributionRequest request)`: ownership check; adds `request.amount()` to `contributedAmount`; if `contributedAmount >= targetAmount`, sets `status = ACHIEVED`; returns updated `GoalResponse`. Annotated `@Transactional`.
Computed fields: `remainingAmount = max(0, targetAmount - contributedAmount)` using `BigDecimal.max`; `progressPercentage = (contributedAmount / targetAmount).multiply(100)` scaled to 2 decimal places with `HALF_UP`; guard: if `targetAmount.compareTo(BigDecimal.ZERO) == 0`, return `BigDecimal.ZERO` for `progressPercentage`.

**Output file**: `finance-tracker/src/main/java/com/financetracker/service/GoalService.java`

---

### T053 — Implement GoalController

**Phase**: 5 — Budget and Goal Management
**Parallel**: No (depends on T052)

Implement `GoalController.java` with `@RestController`, `@RequestMapping("/api/v1/goals")`:
- `GET /api/v1/goals` → `200 OK`
- `POST /api/v1/goals` → `201 Created`
- `GET /api/v1/goals/{id}` → `200 OK`
- `PUT /api/v1/goals/{id}` → `200 OK`
- `DELETE /api/v1/goals/{id}` → `204 No Content`
- `POST /api/v1/goals/{id}/contributions` → `200 OK` with updated `GoalResponse`
Annotate all endpoints with SpringDoc annotations.

**Output file**: `finance-tracker/src/main/java/com/financetracker/controller/GoalController.java`

---

## Category: Tests — Budget (TDD)

---

### T054 — Write BudgetRepository @DataJpaTest [P]

**Phase**: 5 — Budget and Goal Management (Tests)
**Parallel**: Yes — can run alongside T055, T056, T057, T058

Write `@DataJpaTest` tests for `BudgetRepository`:
- `findByUserIdWithSpentAmount` returns correct `spentAmount` after known EXPENSE transactions are inserted for that category and period
- `spentAmount` is `0` when no transactions exist for the budget period
- `existsByUserIdAndCategoryIdAndMonthAndYear` returns true for duplicate, false for unique
- `BigDecimal` round-trip for `limitAmount` preserves 4 decimal places; assert `compareTo` equality

**Output file**: `finance-tracker/src/test/java/com/financetracker/repository/BudgetRepositoryTest.java`

---

### T055 — Write BudgetService unit tests [P]

**Phase**: 5 — Budget and Goal Management (Tests)
**Parallel**: Yes — can run alongside T054, T056, T057, T058

Write JUnit 5 + Mockito unit tests for `BudgetService`:
- `createBudget`: success path persists and returns `BudgetResponse`
- `createBudget`: throws `DuplicateResourceException` when same user/category/month/year already exists
- `getBudgets`: `overLimit` is `false` when `spentAmount < limitAmount`
- `getBudgets`: `overLimit` is `true` when `spentAmount > limitAmount`
- `getBudgets`: `overLimit` is `false` (not exceeded) when `spentAmount == limitAmount`
- `updateBudget`: updates `limitAmount` and returns updated response
- `deleteBudget`: calls repository delete
- BigDecimal arithmetic: no floating-point errors in computed fields
Mock `BudgetRepository`, `BudgetMapper`.

**Output file**: `finance-tracker/src/test/java/com/financetracker/service/BudgetServiceTest.java`

---

### T056 — Write BudgetController integration tests [P]

**Phase**: 5 — Budget and Goal Management (Tests)
**Parallel**: Yes — can run alongside T054, T055, T057, T058

Write `@SpringBootTest` + `MockMvc` integration tests for `BudgetController`:
- POST with valid body → 201
- POST with duplicate category/month/year for same user → 409
- POST with invalid `month=13` → 400
- GET list → 200 includes `spentAmount` and `overLimit` fields
- GET `/{id}` → 200
- PUT `/{id}` → 200
- DELETE `/{id}` → 204
- All 401 paths when no JWT
- Error response schema conforms to `{ timestamp, status, error, message, path }` on all error paths

**Output file**: `finance-tracker/src/test/java/com/financetracker/controller/BudgetControllerTest.java`

---

## Category: Tests — Goal (TDD)

---

### T057 — Write GoalService unit tests [P]

**Phase**: 5 — Budget and Goal Management (Tests)
**Parallel**: Yes — can run alongside T054, T055, T056, T058

Write JUnit 5 + Mockito unit tests for `GoalService`:
- `createGoal`: persists with `IN_PROGRESS` and `contributedAmount = 0`
- `getGoal`: `remainingAmount` = `targetAmount - contributedAmount` when not yet achieved
- `getGoal`: `remainingAmount` = `0` when `contributedAmount > targetAmount`
- `getGoal`: `progressPercentage` computed with `HALF_UP` rounding to 2 decimal places
- `addContribution`: increments `contributedAmount`
- `addContribution`: sets status to `ACHIEVED` when `contributedAmount >= targetAmount`
- `addContribution`: status remains `ACHIEVED` after additional contributions when already achieved
- BigDecimal division guard: `progressPercentage` returns `0.00` when `targetAmount = 0`
Mock `GoalRepository`, `GoalMapper`.

**Output file**: `finance-tracker/src/test/java/com/financetracker/service/GoalServiceTest.java`

---

### T058 — Write GoalController integration tests [P]

**Phase**: 5 — Budget and Goal Management (Tests)
**Parallel**: Yes — can run alongside T054, T055, T056, T057

Write `@SpringBootTest` + `MockMvc` integration tests for `GoalController`:
- POST `/goals` with valid body → 201
- POST `/goals` with missing `name` → 400
- GET `/goals` → 200 with `progressPercentage` and `remainingAmount` fields present
- GET `/goals/{id}` → 200
- PUT `/goals/{id}` → 200
- DELETE `/goals/{id}` → 204
- POST `/goals/{id}/contributions` with positive amount → 200; verify `contributedAmount` increased
- POST `/goals/{id}/contributions` with negative amount → 400
- POST `/goals/{id}/contributions` that meets target → 200; verify `status = ACHIEVED`
- All 401 paths when no JWT

**Output file**: `finance-tracker/src/test/java/com/financetracker/controller/GoalControllerTest.java`

---

## Category: Core — Reporting (Phase 6)

> Prerequisites: Phases 1–5 complete and all phase gates passing.

---

### T059 — Implement Report DTOs [P]

**Phase**: 6 — Reporting and Quality Gate
**Parallel**: Yes — can run alongside T060

Implement the following DTOs:
- `CategoryBreakdown.java`: Java 17 record with fields `categoryId` (Long), `categoryName` (String), `income` (BigDecimal), `expense` (BigDecimal)
- `SummaryResponse.java`: class (or record) with fields `startDate`, `endDate`, `totalIncome` (BigDecimal), `totalExpenses` (BigDecimal), `netBalance` (BigDecimal), `categoryBreakdowns` (List<CategoryBreakdown>)
- `MonthlyTrendResponse.java`: class (or record) with fields `month` (int), `year` (int), `totalIncome` (BigDecimal), `totalExpenses` (BigDecimal), `netBalance` (BigDecimal)

**Output files**:
- `finance-tracker/src/main/java/com/financetracker/dto/report/CategoryBreakdown.java`
- `finance-tracker/src/main/java/com/financetracker/dto/report/SummaryResponse.java`
- `finance-tracker/src/main/java/com/financetracker/dto/report/MonthlyTrendResponse.java`

---

### T060 — Implement ReportService aggregation queries [P]

**Phase**: 6 — Reporting and Quality Gate
**Parallel**: Yes — can run alongside T059 (queries are independent of DTO shape during development)

Extend `TransactionRepository` (or create a dedicated `ReportRepository`) with JPQL aggregation queries:
- A query for summary: groups EXPENSE and INCOME transactions by category within a date range, returning `categoryId`, `categoryName`, `type`, and `SUM(amount)` per group
- A query for monthly trend: groups transactions by `YEAR(t.date)` and `MONTH(t.date)`, returning year, month, type, and `SUM(amount)` per group, ordered by year and month ascending
Raw SQL is permitted only if JPQL is insufficient per foundation.

**Output file**: `finance-tracker/src/main/java/com/financetracker/repository/TransactionRepository.java` (extended) or `finance-tracker/src/main/java/com/financetracker/repository/ReportRepository.java` (new interface)

---

### T061 — Implement ReportService

**Phase**: 6 — Reporting and Quality Gate
**Parallel**: No (depends on T059, T060)

Implement `ReportService.java` with `@Service`:
- `getSummary(Long userId, LocalDate startDate, LocalDate endDate)`:
  - If `startDate` or `endDate` is null, default to the first and last day of the current calendar month
  - Execute aggregation query; aggregate `totalIncome`, `totalExpenses`, `netBalance = totalIncome - totalExpenses` using `BigDecimal` with `HALF_UP`
  - Build `List<CategoryBreakdown>` from per-category results
  - Return `SummaryResponse`
- `getMonthlyTrend(Long userId, LocalDate startDate, LocalDate endDate)`:
  - If dates are null, default to the current calendar month (or a sensible default range)
  - Execute monthly grouping query; compute `netBalance` per month
  - Return `List<MonthlyTrendResponse>` ordered by year-month ascending
All `BigDecimal` arithmetic uses `HALF_UP` rounding.

**Output file**: `finance-tracker/src/main/java/com/financetracker/service/ReportService.java`

---

### T062 — Implement ReportController

**Phase**: 6 — Reporting and Quality Gate
**Parallel**: No (depends on T061)

Implement `ReportController.java` with `@RestController`, `@RequestMapping("/api/v1/reports")`:
- `GET /api/v1/reports/summary` → `200 OK` with `SummaryResponse`; accepts optional `startDate` and `endDate` query parameters as `LocalDate` (use `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)`)
- `GET /api/v1/reports/monthly-trend` → `200 OK` with `List<MonthlyTrendResponse>`; same optional date params
Annotate all endpoints with `@Operation`, `@ApiResponse`, `@Schema`.

**Output file**: `finance-tracker/src/main/java/com/financetracker/controller/ReportController.java`

---

## Category: Tests — Reporting (TDD)

---

### T063 — Write ReportService unit tests [P]

**Phase**: 6 — Reporting and Quality Gate (Tests)
**Parallel**: Yes — can run alongside T064

Write JUnit 5 + Mockito unit tests for `ReportService`:
- `getSummary` with explicit date range: returns correct `totalIncome`, `totalExpenses`, `netBalance`
- `getSummary` with no dates provided: defaults to current calendar month first/last days
- `getSummary` category breakdown: per-category income and expense totals are correct
- `getSummary` with no transactions in range: returns all zeros and empty `categoryBreakdowns`
- `getMonthlyTrend`: returns months in ascending order
- `getMonthlyTrend`: each month's `netBalance = totalIncome - totalExpenses`
- `BigDecimal` arithmetic: no floating-point errors; use `compareTo` for assertions
Mock `TransactionRepository` / `ReportRepository`.

**Output file**: `finance-tracker/src/test/java/com/financetracker/service/ReportServiceTest.java`

---

### T064 — Write ReportController integration tests [P]

**Phase**: 6 — Reporting and Quality Gate (Tests)
**Parallel**: Yes — can run alongside T063

Write `@SpringBootTest` + `MockMvc` integration tests for `ReportController`:
- GET `/reports/summary` with no dates → 200, defaults to current month, all numeric fields present
- GET `/reports/summary` with explicit `startDate` and `endDate` → 200 with correct data
- GET `/reports/summary` for user with no transactions → 200 with all zero totals
- GET `/reports/monthly-trend` with no dates → 200 with months in ascending order
- GET `/reports/monthly-trend` spanning multiple months → correct grouping
- All 401 paths when no JWT

**Output file**: `finance-tracker/src/test/java/com/financetracker/controller/ReportControllerTest.java`

---

## Category: Polish — OpenAPI, Quality Gate, and Static Analysis

> Prerequisites: All Phases 1–6 tasks complete and their gates passing.

---

### T065 — Add SpringDoc OpenAPI annotations to all controllers [P]

**Phase**: 6 — Polish
**Parallel**: Yes — each controller is independent; tasks T065a–T065g can run in parallel

Review and add `@Operation(summary=...)`, `@ApiResponse` for all documented status codes, and `@Schema` on all DTO fields across all seven controllers:

- T065a: `AuthController` — ensure register (201, 400, 409) and login (200, 400, 401) responses are documented
- T065b: `UserController` — ensure GET/PUT (200, 401, 404), PUT password (204, 400, 401), DELETE (204, 401) are documented
- T065c: `TransactionController` — ensure all five endpoints and filter/pagination params are documented
- T065d: `CategoryController` — ensure all three endpoints and error codes (403, 409) are documented
- T065e: `BudgetController` — ensure all five endpoints and computed fields (`spentAmount`, `overLimit`) are documented
- T065f: `GoalController` — ensure all six endpoints and computed fields (`progressPercentage`, `remainingAmount`, `status`) are documented
- T065g: `ReportController` — ensure both endpoints with optional date params are documented

**Output files**: All seven controller files listed above (edits only)

---

### T066 — Write OpenAPI integration test

**Phase**: 6 — Polish
**Parallel**: No (depends on T065a–T065g being complete)

Write an integration test that:
- Loads the generated OpenAPI spec from `/v3/api-docs`
- Asserts that all 27 expected endpoint paths are present
- Asserts that the error response shape `{ timestamp, status, error, message, path }` is defined in the schema components
- Asserts that JWT bearer auth scheme is defined

**Output file**: `finance-tracker/src/test/java/com/financetracker/controller/OpenApiIntegrationTest.java`

---

### T067 — Run full JaCoCo coverage report and address coverage gaps

**Phase**: 6 — Polish
**Parallel**: No (depends on all test tasks complete)

Execute `./mvnw verify` to generate the JaCoCo coverage report. Review the report and:
- Confirm overall line coverage >= 80% (build gate enforces this; the build will fail if not met)
- Confirm branch coverage >= 70%
- For any class or method below threshold, add targeted unit tests to close the gap
- Document any intentional coverage exclusions (e.g., generated code) with `@Generated` or JaCoCo exclusion config in `pom.xml`

**Output**: Updated test files as needed to meet coverage gate; updated `pom.xml` exclusion config if applicable

---

### T068 — Run Checkstyle and resolve all violations [P]

**Phase**: 6 — Polish
**Parallel**: Yes — can run alongside T069, T070

Execute `./mvnw checkstyle:check`. Confirm zero violations. For any violation:
- If a `double` or `float` is found in a financial class, replace with `BigDecimal`
- If method length exceeds 30 lines, refactor into smaller private methods
- If class length exceeds 300 lines, decompose into separate classes
- For any `@SuppressWarnings` annotations added, include a comment explaining the justification

---

### T069 — Run SpotBugs and resolve all high-priority findings [P]

**Phase**: 6 — Polish
**Parallel**: Yes — can run alongside T068, T070

Execute `./mvnw spotbugs:check`. Confirm zero high-priority findings. Common issues to look for:
- Null pointer dereferences
- Resource leaks
- Incorrect use of `equals` on `BigDecimal` (should use `compareTo`)
- Thread safety issues

---

### T070 — Run ArchUnit tests and confirm all layer rules pass [P]

**Phase**: 6 — Polish
**Parallel**: Yes — can run alongside T068, T069

Execute `./mvnw test -Dtest=ArchitectureTest`. Confirm all assertions in `ArchitectureTest.java` pass:
- No controller directly depends on a repository
- No service imports HTTP types
- All entities in the `model` package
- All repositories in the `repository` package
- Cross-layer dependency direction is Controller → Service → Repository only

---

### T071 — Perform manual walkthrough of error response schema

**Phase**: 6 — Polish
**Parallel**: No (depends on all integration tests passing)

Using Swagger UI or a local test run, manually trigger at least one validation failure and one 404 response per resource domain (auth, users, transactions, categories, budgets, goals, reports) and verify the response body matches:
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Bad Request",
  "message": "...",
  "path": "..."
}
```
Document findings. If any resource returns a non-conforming error format, fix the `GlobalExceptionHandler`.

---

### T072 — Verify Swagger UI renders all 27 endpoints [P]

**Phase**: 6 — Polish
**Parallel**: Yes — can run alongside T071

Start the application in dev profile and navigate to `http://localhost:8080/swagger-ui.html`. Confirm all 27 endpoints are present and correctly documented:

| Resource | Endpoints |
|---|---|
| Auth | POST /register, POST /login |
| Users | GET /me, PUT /me, PUT /me/password, DELETE /me |
| Transactions | GET, POST, GET /{id}, PUT /{id}, DELETE /{id} |
| Categories | GET, POST, DELETE /{id} |
| Budgets | GET, POST, GET /{id}, PUT /{id}, DELETE /{id} |
| Goals | GET, POST, GET /{id}, PUT /{id}, DELETE /{id}, POST /{id}/contributions |
| Reports | GET /summary, GET /monthly-trend |

Total: 27 endpoints. Document any missing endpoints and add the missing SpringDoc annotations.

---

### T073 — Add Javadoc to all public service and controller methods [P]

**Phase**: 6 — Polish
**Parallel**: Yes — each service/controller is independent

Add Javadoc (`/** ... */`) to all public methods on all service and controller classes describing purpose, parameters, and return value. This applies to:
- `AuthService`, `UserService`, `CategoryService`, `TransactionService`, `BudgetService`, `GoalService`, `ReportService`
- `AuthController`, `UserController`, `CategoryController`, `TransactionController`, `BudgetController`, `GoalController`, `ReportController`

---

### T074 — Final foundation compliance verification [P]

**Phase**: 6 — Polish
**Parallel**: Yes — can run alongside T073

Review the Foundation Compliance Check in `plan.md` and verify all checkboxes can now be marked complete:
- [ ] All monetary fields use `BigDecimal` with `precision=19, scale=4`
- [ ] No `double` or `float` in financial model classes
- [ ] All write operations wrapped in `@Transactional`
- [ ] Rounding mode is `BigDecimal.HALF_UP` throughout
- [ ] Classes reside in correct layer packages
- [ ] Service classes carry `@Service`; no business logic in controllers
- [ ] All service methods have JUnit 5 + Mockito unit tests
- [ ] All controller endpoints have `@SpringBootTest` + `MockMvc` integration tests
- [ ] Repository layer tested with `@DataJpaTest`
- [ ] Line coverage >= 80%; branch coverage >= 70%
- [ ] All endpoints annotated with `@Operation`, `@ApiResponse`, `@Schema`
- [ ] Error responses conform to `{ timestamp, status, error, message, path }` schema
- [ ] All endpoints require JWT authentication except public auth endpoints
- [ ] Passwords stored as BCrypt hashes only
- [ ] JWT secret externalized via environment variable
- [ ] CORS explicitly configured; no wildcard origins in production
- [ ] Java 17 records used for all DTO and value types
- [ ] All public service and controller methods carry Javadoc

**Output**: Updated Foundation Compliance Check section in `specs/20260309-finance-tracker-api/plan.md`

---

### T075 — Create CI pipeline configuration [P]

**Phase**: 6 — Polish
**Parallel**: Yes — independent of application code

Create a GitHub Actions CI workflow that:
- Triggers on pull requests and pushes to `main`
- Runs `./mvnw verify` (includes unit tests, integration tests, JaCoCo gate, Checkstyle, SpotBugs)
- Publishes JaCoCo coverage report as a workflow artifact
- Enables repository secrets scanning

**Output file**: `finance-tracker/.github/workflows/ci.yml`

---

### T076 — Integration test: BigDecimal precision round-trip assertion [P]

**Phase**: 6 — Polish
**Parallel**: Yes — targeted test file, independent of other polish tasks

Write a dedicated integration test (or extend an existing `@DataJpaTest`) that:
- Persists a transaction with `amount = 1234.5678` (4 decimal places)
- Retrieves it via the repository
- Asserts `retrieved.getAmount().compareTo(new BigDecimal("1234.5678")) == 0`
This directly satisfies Foundation Principle 1 compliance verification for BigDecimal round-trip integrity.

**Output file**: `finance-tracker/src/test/java/com/financetracker/repository/TransactionRepositoryTest.java` (add test method to existing file)

---

### T077 — Security integration test: optimistic locking path on Budget [P]

**Phase**: 6 — Polish
**Parallel**: Yes — independent targeted test

Write an integration test that verifies the optimistic locking path on `Budget`:
- Insert a budget
- Simulate two concurrent updates by:
  1. Loading the budget entity twice (two separate repository calls to get two detached instances with the same `@Version`)
  2. Saving the first update
  3. Attempting to save the second update and asserting it produces a `409 Conflict` response (via `OptimisticLockException` mapped in `GlobalExceptionHandler`)
This satisfies the high-risk mitigation item for optimistic locking from `plan.md`.

**Output file**: `finance-tracker/src/test/java/com/financetracker/controller/BudgetControllerTest.java` (add test method to existing file)

---

## Dependency Graph

```
T001 (Init Project)
  └── T002 (pom.xml plugins)
        ├── T003 [P] (application.yml files)
        ├── T004 [P] (Liquibase: users changeset)
        ├── T005 [P] (Liquibase: categories, transactions)
        ├── T006 [P] (Liquibase: budgets, goals)
        ├── T007 [P] (Checkstyle config)
        ├── T008 [P] (GlobalExceptionHandler)
        ├── T009 [P] (PaginationConfig)
        ├── T010 [P] (OpenApiConfig)
        └── T011   (ArchUnit test)
              │
              ▼ PHASE 1 GATE (build + migrations + ArchUnit pass)
              │
        ┌─────┴────────────────────────────────────┐
        T012 [P]       T013 [P]       T016 [P]       T017 [P]
        (User entity)  (JwtService)   (Auth DTOs)    (User DTOs)
             │              │
             └──────┬───────┘
                   T014 [P] (JwtFilter + UserDetailsService)
                         │
                        T015 (SecurityConfig + CorsConfig)
                         │
              T018 [P]  T019        T020
              (UserMap) (AuthSvc)   (UserSvc)
                    │       │           │
                   T021  T022      T023–T026 [P] (Tests)
                         │
                        T027 (SecurityIntegrationTest)
                              │
                              ▼ PHASE 2 GATE
                              │
                    T028 [P]  T029 [P]  T030 [P]
                    (Category  (Cat    (Cat DTOs
                     entity)  Repo)    + Mapper)
                         └──────┬──────┘
                               T031 (CategoryService)
                                │
                               T032 (CategoryController)
                                │
                         T033–T035 [P] (Category Tests)
                              │
                              ▼ PHASE 3 GATE
                              │
                    T036 [P]  T037 [P]  T038 [P]
                    (Txn      (Txn      (Txn DTOs
                     entity)  Repo)     + Mapper)
                         └──────┬──────┘
                               T039 (TransactionService)
                                │
                               T040 (TransactionController)
                                │
                         T041–T043 [P] (Transaction Tests)
                              │
                              ▼ PHASE 4 GATE
                              │
         T044 [P]  T045 [P]  T046 [P]  T047 [P]  T048 [P]  T049 [P]
         (Budget   (Budget   (Budget   (Goal      (Goal     (Goal
          entity)  Repo)     DTOs)     entity)    Repo)     DTOs)
              └──────┬───────┘              └──────┬───────┘
                    T050 (BudgetService)           T052 (GoalService)
                     │                              │
                    T051 (BudgetController)        T053 (GoalController)
                     │                              │
               T054–T056 [P]                  T057–T058 [P]
               (Budget Tests)                 (Goal Tests)
                              │
                              ▼ PHASE 5 GATE
                              │
                    T059 [P]  T060 [P]
                    (Report   (Report
                     DTOs)    Queries)
                         └──────┬──────┘
                               T061 (ReportService)
                                │
                               T062 (ReportController)
                                │
                         T063–T064 [P] (Report Tests)
                              │
                              ▼ PHASE 6 GATE
                              │
         T065a–g [P]  T066  T067  T068 [P]  T069 [P]  T070 [P]
         (OpenAPI    (API   (JaCo- (Check-   (SpotBugs) (ArchUnit
          Annots.)   Test)  Co)    style)                final)
                │
         T071  T072 [P]  T073 [P]  T074 [P]  T075 [P]  T076 [P]  T077 [P]
         (Error (Swagger  (Javadoc) (Foundation (CI)    (BigDec.  (OptLock
          walk) check)             Compliance)          RT test)  test)
```

---

## Parallel Execution Examples

### Phase 1 — Setup tasks that can run in parallel

The following tasks have no dependencies on each other and can be developed simultaneously:

```
T003: Create application YAML files
T004: Create users Liquibase changeset
T005: Create categories/transactions Liquibase changesets
T006: Create budgets/goals Liquibase changesets
T007: Create Checkstyle config
T008: Implement exception infrastructure
T009: Implement PaginationConfig
T010: Implement OpenApiConfig
```

### Phase 2 — Entity and infrastructure parallel tracks

```
Track A: T012 (User entity) → T014 (JwtFilter) → T015 (SecurityConfig)
Track B: T013 (JwtService) → T014 (JwtFilter) → T015 (SecurityConfig)
Track C: T016 (Auth DTOs) — independent
Track D: T017 (User DTOs) — independent
Track E: T018 (UserMapper) — after T012 + T017
```

### Phase 2 — Test tasks that can run in parallel after implementations

```
T023: UserRepository @DataJpaTest
T024: AuthService unit tests
T025: UserService unit tests
T026: Auth/User controller integration tests
```
(All of T023–T026 are independent of each other — different test files.)

### Phase 5 — Budget and Goal parallel tracks

```
Track A: T044 → T045 → T046 → T050 → T051
Track B: T047 → T048 → T049 → T052 → T053
Tests:   T054, T055, T056, T057, T058 (all parallel)
```

### Phase 6 — Polish tasks that can run in parallel

```
T065a–g: OpenAPI annotations for each controller (7 parallel sub-tasks)
T068: Checkstyle
T069: SpotBugs
T070: ArchUnit final run
T072: Swagger UI verification
T073: Javadoc
T074: Foundation compliance
T075: CI pipeline
T076: BigDecimal round-trip test
T077: Optimistic locking test
```

---

## Validation Checklist

### Coverage — All API Contracts Have Corresponding Tests

| Endpoint | Unit Test | Integration Test | Repository Test |
|---|---|---|---|
| POST /auth/register | T024 | T026 | T023 |
| POST /auth/login | T024 | T026 | — |
| GET /users/me | T025 | T026 | — |
| PUT /users/me | T025 | T026 | — |
| PUT /users/me/password | T025 | T026 | — |
| DELETE /users/me | T025 | T026 | — |
| GET /transactions | T042 | T043 | T041 |
| POST /transactions | T042 | T043 | T041 |
| GET /transactions/{id} | T042 | T043 | — |
| PUT /transactions/{id} | T042 | T043 | — |
| DELETE /transactions/{id} | T042 | T043 | — |
| GET /categories | T034 | T035 | T033 |
| POST /categories | T034 | T035 | T033 |
| DELETE /categories/{id} | T034 | T035 | T033 |
| GET /budgets | T055 | T056 | T054 |
| POST /budgets | T055 | T056 | T054 |
| GET /budgets/{id} | T055 | T056 | — |
| PUT /budgets/{id} | T055 | T056 | — |
| DELETE /budgets/{id} | T055 | T056 | — |
| GET /goals | T057 | T058 | — |
| POST /goals | T057 | T058 | — |
| GET /goals/{id} | T057 | T058 | — |
| PUT /goals/{id} | T057 | T058 | — |
| DELETE /goals/{id} | T057 | T058 | — |
| POST /goals/{id}/contributions | T057 | T058 | — |
| GET /reports/summary | T063 | T064 | T060 |
| GET /reports/monthly-trend | T063 | T064 | T060 |

Total endpoints: 27. All covered.

### Coverage — All Entities Have Creation Tasks

| Entity | Task |
|---|---|
| User | T012 |
| Category | T028 |
| Transaction | T036 |
| Budget | T044 |
| Goal | T047 |

### Coverage — All Design Document Sections Are Covered

| Plan Section | Tasks |
|---|---|
| Phase 1: Scaffolding | T001–T011 |
| Phase 2: Security and Auth | T012–T027 |
| Phase 3: Category Management | T028–T035 |
| Phase 4: Transaction Management | T036–T043 |
| Phase 5: Budget and Goal Management | T044–T058 |
| Phase 6: Reporting and Quality Gate | T059–T077 |
| Risk: spentAmount computation accuracy | T054, T055, T056 |
| Risk: Optimistic locking conflicts | T077 |
| Risk: BigDecimal division by zero | T057 |
| Risk: Cascading deletes on deleteAccount | T025, T026 |
| Risk: BigDecimal round-trip precision | T076 |
| Risk: Pagination max-size enforcement | T043, T056 |

### TDD Ordering Verification

All test tasks precede or are co-located with their implementation tasks within the same phase. Tests are written before or concurrent with implementation per TDD principles:
- Phase 2 tests (T023–T027) are written alongside and after Phase 2 implementations (T012–T022)
- Phase 3 tests (T033–T035) alongside Phase 3 implementations (T028–T032)
- Phase 4 tests (T041–T043) alongside Phase 4 implementations (T036–T040)
- Phase 5 tests (T054–T058) alongside Phase 5 implementations (T044–T053)
- Phase 6 tests (T063–T064) alongside Phase 6 implementations (T059–T062)

### Foundation Compliance Mapping

| Foundation Principle | Enforcing Tasks |
|---|---|
| Financial Data Integrity (BigDecimal, HALF_UP) | T036, T044, T047, T050, T052, T061, T068, T076 |
| Layered Separation of Concerns | T011, T070 |
| Test Coverage >= 80% line / 70% branch | T067 |
| Explicit API Contracts (SpringDoc) | T065a–g, T066, T072 |
| Security by Default (JWT, BCrypt, CORS) | T013–T015, T019, T027 |
| Clean Code and Java 17 Idioms (records, Javadoc) | T016, T017, T030, T038, T046, T049, T059, T073 |

---

*Based on Foundation v1.0.0 — See `directive/foundation.md` for complete principles and governance.*
*Plan source: `specs/20260309-finance-tracker-api/plan.md`*
*Spec source: `specs/20260309-finance-tracker-api/spec.md`*
