<!-- Bootstrapped Copilot/agent instructions for the Finance Tracker repo -->
# Copilot / Agent Instructions — Finance Tracker

Purpose
- Provide concise, actionable guidance to AI assistants working on this repository.
- Highlight project commands, conventions, architecture boundaries, and common pitfalls.

How to use
- Prefer small, well-scoped edits. Open a short PR for any non-trivial change.
- When asked to run or build, use Docker Compose by default; fall back to local Maven when explicitly requested.

Repository quick facts
- Language: Java 17 (Spring Boot 3.2)
- Build: Maven (`mvn`)
- DB: PostgreSQL 15 + Liquibase migrations
- Frontend: Single-page static files served from `src/main/resources/static`

Primary commands
- Build and run with Docker Compose (recommended):

  docker compose up --build

- Run locally (dev profile):

  mvn spring-boot:run -Dspring-boot.run.profiles=dev

- Run tests and check coverage:

  mvn verify

Project conventions and expectations
- Keep a layered architecture: `controller` → `service` → `repository`.
- Use `BigDecimal` for money; round HALF_UP.
- MapStruct is used for DTO mapping; preserve generated mapping classes.
- Tests must meet the JaCoCo gates (80% line, 70% branch) where practical.
- Checkstyle and SpotBugs are enabled in CI; adhere to their rules on new code.

Important files and locations
- Application entry: `src/main/java/com/financetracker/FinanceTrackerApplication.java`
- Configs: `src/main/resources/application*.yml`
- Liquibase migrations: `src/main/resources/db/changelog/`
- Frontend single-page app: `src/main/resources/static/index.html` and `src/main/resources/static/js/`
- Build manifest: `pom.xml`

Known pitfalls / guidance
- The app expects environment variables for DB and `APP_JWT_SECRET`.
- When running locally, ensure a Postgres instance is available or switch to the `test` profile that uses H2 for unit tests.
- Static frontend is served by Spring Boot — avoid introducing a separate frontend build step unless asked.

When editing / creating files
- Keep modifications minimal and well-scoped. Add failing unit tests to reproduce bugs before fixing.
- Run `mvn -DskipTests=false verify` locally when changing behavior that affects tests or coverage.

Example prompts for maintainers
- "Add an endpoint `GET /api/v1/reports/annual` that returns yearly summary; include controller, service, repository, DTO, tests, and OpenAPI docs."
- "Fix failing `TransactionsControllerTest` that asserts pagination; run tests and update code and tests so they pass." 
- "Create a migration to add `notes` (text) column to `transactions` table, and update entity, DTO, mapper, and tests."

Next agent customizations to consider
- Add an `applyTo` pattern for `src/main/resources/static` to allow a lightweight frontend agent that only edits JS/HTML/CSS.
- Provide a `CI assistant` instruction set that knows how to run the Maven lifecycle and interpret JaCoCo/Checkstyle output.

Contact / context
- See the repo README for usage and environment variable examples.
  
<!-- End -->