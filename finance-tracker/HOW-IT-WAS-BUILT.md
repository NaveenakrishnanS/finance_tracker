# How Finance Tracker Was Built with Claude Code and Claude Code Buddy

This document explains the complete development journey of the Finance Tracker application —
from a single sentence of user input to a fully dockerized, production-ready full-stack application —
using **Claude Code** and the **Claude Code Buddy** plugin.

---

## What Is Claude Code?

**Claude Code** is Anthropic's official AI-powered CLI for software engineering. It runs in your
terminal and acts as an interactive coding agent — it can read files, write code, run commands,
search codebases, and execute multi-step tasks autonomously. It is powered by Claude (claude-sonnet-4-6).

## What Is Claude Code Buddy?

**Claude Code Buddy** is a plugin for Claude Code that adds a structured software delivery workflow
on top of the AI agent. It introduces a chain of specialized slash commands (skills) that guide a
project from initial idea all the way through implementation:

| Buddy Skill | Purpose |
|---|---|
| `/buddy:foundation` | Establishes the project's core principles and architecture rules |
| `/buddy:spec` | Converts a plain-language description into a formal feature specification |
| `/buddy:plan` | Turns a specification into a phased implementation plan |
| `/buddy:tasks` | Breaks a plan into granular, ordered, executable tasks |
| `/buddy:implement` | Executes the task list file by file, phase by phase |

Each skill produces a structured Markdown document that becomes the input for the next skill,
creating a traceable paper trail from idea to working code.

---

## The Development Journey

### Step 0 — Starting Point

The entire project began with a single sentence:

> _"finance tracker using spring boot and java17"_

That's it. No architecture diagram. No API design. No UI mockup. Claude Code Buddy took that
sentence and drove the entire delivery pipeline.

---

### Step 1 — Project Foundation (`/buddy:foundation`)

**What happened:** Claude Code Buddy generated `directive/foundation.md` — a governing document
that defines the project's purpose, core architectural principles, technology choices, and
compliance rules that all subsequent documents must follow.

**Key decisions made at this stage:**
- Java 17 + Spring Boot 3.2 as the backend platform
- PostgreSQL as the database with Liquibase for schema migrations
- Stateless JWT authentication (Auth0 library, 2-hour expiry)
- `BigDecimal` with `HALF_UP` rounding for all monetary values
- Layered architecture: Controller → Service → Repository
- MapStruct for DTO mapping, SpringDoc for OpenAPI documentation

**Output:** `directive/foundation.md` (v1.0.0)

---

### Step 2 — API Specification (`/buddy:spec`)

**What happened:** Claude Code Buddy read the foundation and the user's input, then generated
a complete formal specification for the REST API backend.

**What the spec defined:**
- All user scenarios and acceptance criteria (register, login, CRUD for every resource)
- Functional requirements for every endpoint
- Non-functional requirements (security, pagination, error format, BigDecimal serialization)
- Database entity relationships
- API contract: request/response shapes, HTTP status codes, error response schema
- Scope boundaries (what is explicitly out of scope)

**Output:** `specs/20260309-finance-tracker-api/spec.md`

---

### Step 3 — API Implementation Plan (`/buddy:plan`)

**What happened:** Claude Code Buddy read the spec and produced a phased implementation plan
describing exactly how to build the backend — which files to create, in what order, and why.

**What the plan covered:**
- Full project file structure (every Java class and configuration file)
- Phase-by-phase build order: Setup → Security → Models → Repositories → Services → Controllers → Tests
- Technology rationale for each dependency in `pom.xml`
- Database schema design and Liquibase migration strategy
- JWT filter chain configuration
- Pagination strategy (Spring `Pageable`, max page size 25)

**Output:** `specs/20260309-finance-tracker-api/plan.md`

---

### Step 4 — API Task Breakdown (`/buddy:tasks`)

**What happened:** Claude Code Buddy converted the implementation plan into a numbered task list
with explicit file targets, dependency ordering, and parallel execution markers.

**Task structure:**
- 42 tasks organized into phases (Setup, Security, Models, Services, Controllers, Tests, Polish)
- Each task listed exactly which files to create or modify
- Tasks marked `[P]` could run in parallel; others were strictly sequential
- Checkboxes (`- [ ]`) for progress tracking, updated to `- [X]` as each task completed

**Output:** `specs/20260309-finance-tracker-api/tasks.md`

---

### Step 5 — API Implementation (`/buddy:implement`)

**What happened:** Claude Code invoked the `buddy:task-executor` agent, which read all spec
documents and executed every task in order — creating the entire Spring Boot backend from scratch.

**What was built:**
- `pom.xml` with all dependencies (Spring Boot, Security, JPA, Liquibase, PostgreSQL, JWT, MapStruct, SpringDoc)
- Liquibase migration scripts: users, categories, transactions, budgets, goals tables
- Default category seeding (Food, Transport, Housing, Healthcare, etc.)
- JPA entities with validation annotations
- Spring Data repositories
- Service layer with business logic (budget over-limit detection, goal progress calculation)
- REST controllers for all resources
- JWT filter, `UserDetailsService`, Spring Security configuration
- Global exception handler (`@RestControllerAdvice`) with standardized error response format
- MapStruct mappers for all entities
- `application.yml` / `application-dev.yml` / `application-prod.yml` profiles
- Checkstyle and SpotBugs configuration

**Output:** The complete `finance-tracker/src/main/java/` tree and all resource files

---

### Step 6 — Frontend Specification (`/buddy:spec`)

**What happened:** The same `/buddy:spec` workflow was run again, this time for the frontend.
The spec described a no-framework, no-build-tool browser client for the API that was just built.

**Key decisions made at this stage:**
- Single `index.html` with hash-based routing (`#login`, `#dashboard`, `#transactions`, etc.)
- Plain HTML5 + vanilla JavaScript ES2020+ only — no React, Vue, Angular, or build tools
- Pico.css via CDN for baseline responsive styling
- Chart.js via CDN for the monthly trend chart
- Files placed inside `src/main/resources/static/` so Spring Boot serves them on the same origin
  (zero CORS configuration required)
- JWT stored in `localStorage` under the key `ft_token`
- Centralized `apiFetch()` wrapper for all API calls with global 401 handling

**Output:** `specs/20260309-finance-tracker-frontend/spec.md`

---

### Step 7 — Frontend Implementation Plan (`/buddy:plan`)

**What happened:** Claude Code Buddy generated a plan for the frontend describing the exact file
structure, module responsibilities, and implementation sequence.

**Output:** `specs/20260309-finance-tracker-frontend/plan.md`

---

### Step 8 — Frontend Task Breakdown (`/buddy:tasks`)

**What happened:** 42 tasks were generated covering every view, every form, every edge case, and
every polish requirement from the spec.

**Output:** `specs/20260309-finance-tracker-frontend/tasks.md`

---

### Step 9 — Frontend Implementation (`/buddy:implement`)

**What happened:** The `buddy:task-executor` agent executed all 42 frontend tasks, building the
complete browser client.

**What was built:**

| File | Responsibility |
|---|---|
| `index.html` | Single-page entry point; all 9 view sections, nav bar, CDN script tags |
| `css/styles.css` | View visibility toggling, error/success message styles, badges |
| `js/api.js` | `apiFetch()` wrapper, `API_BASE`, global 401 redirect guard, `formatMoney`, `setLoading` |
| `js/auth.js` | `getToken()`, `setToken()`, `clearToken()`, `redirectIfUnauth()`, `redirectIfAuth()` |
| `js/router.js` | `hashchange` listener, view activation, nav show/hide, logout handler |
| `js/dashboard.js` | Summary fetch, category breakdown table, over-limit budget widget |
| `js/categories.js` | Category list with in-memory cache, add form, delete with 409 error handling |
| `js/transactions.js` | Paginated table, type/category/date filters, dual-mode add/edit form |
| `js/budgets.js` | Budget list with over-limit row highlighting, add/edit limit/delete |
| `js/goals.js` | Goal list with progress bar %, inline contribution form, add/edit/delete |
| `js/trend.js` | Monthly trend table + Chart.js bar chart with destroy/recreate on range change |
| `js/profile.js` | Display name update, password change, account delete with confirmation |

---

### Step 10 — Dockerization (Claude Code direct)

**What happened:** Claude Code (without Buddy) created the production Docker setup directly
in response to the user's request to "dockerize this project production-ready."

**What was created:**

| File | Purpose |
|---|---|
| `Dockerfile` | Multi-stage build: Maven compiles the JAR in a JDK image; the JAR is copied into a minimal JRE image. Runs as a non-root user. |
| `docker-compose.yml` | Orchestrates the app container and a PostgreSQL 15 container. Includes health checks and a named volume for database persistence. |
| `.env.example` | Template listing all required environment variables with descriptions. |
| `.dockerignore` | Excludes `target/`, `.git/`, `.env`, and IDE files from the Docker build context. |

---

## Complete Artifact Trail

```
test_env/
├── directive/
│   └── foundation.md               ← Step 1: Project principles and architecture rules
├── specs/
│   ├── 20260309-finance-tracker-api/
│   │   ├── spec.md                 ← Step 2: API feature specification
│   │   ├── plan.md                 ← Step 3: API implementation plan
│   │   └── tasks.md                ← Step 4: API task list (42 tasks, all completed)
│   └── 20260309-finance-tracker-frontend/
│       ├── spec.md                 ← Step 6: Frontend feature specification
│       ├── plan.md                 ← Step 7: Frontend implementation plan
│       └── tasks.md                ← Step 8: Frontend task list (42 tasks, all completed)
└── finance-tracker/                ← Steps 5, 9, 10: The working application
    ├── src/
    ├── Dockerfile
    ├── docker-compose.yml
    ├── .env.example
    └── README.md
```

**Total tasks executed by AI agents: 84**
**Lines of code written by human: 0**

---

## How to Run

### Option A — Docker (Recommended)

**Prerequisites:** Docker Desktop installed and running.

```bash
cd finance-tracker

# Create your environment file
cp .env.example .env
```

Open `.env` and set the two required secrets:
```env
POSTGRES_PASSWORD=choose_a_strong_password
APP_JWT_SECRET=choose_a_secret_string_at_least_32_characters_long
```

Then start everything:
```bash
docker compose up --build
```

Open your browser at **http://localhost:8080**

To stop:
```bash
docker compose down
```

To stop and wipe all data:
```bash
docker compose down -v
```

---

### Option B — Local Development

**Prerequisites:** Java 17+, Maven 3.8+, PostgreSQL 15 running locally.

```bash
# Set environment variables
export DB_URL=jdbc:postgresql://localhost:5432/finance_tracker
export DB_USERNAME=your_pg_user
export DB_PASSWORD=your_pg_password
export APP_JWT_SECRET=your-secret-at-least-32-chars

# Run with dev profile
cd finance-tracker
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Open your browser at **http://localhost:8080**

API documentation (Swagger UI): **http://localhost:8080/swagger-ui.html**

---

## Use Cases

### 1. Track Daily Spending
Register an account, then log every purchase as a transaction. Assign it a category
(Food & Dining, Transportation, Shopping, etc.) and view your spending history in the
Transactions view, filterable by date range, type, and category.

### 2. Monitor Monthly Budget
Create a budget for each category you want to control — for example, "$500 for Food & Dining
in March 2026." The Budgets view shows how much you've spent vs. your limit in real time.
Budgets that are over their limit are highlighted in red. The Dashboard also surfaces
over-limit budgets as an alert widget so you notice them immediately.

### 3. Build Toward Savings Goals
Create a named savings goal with a target amount and target date (e.g., "Emergency Fund — $5,000
by December 2026"). Each time you set aside money, log a contribution. The Goals view tracks
your progress as a percentage and marks the goal as ACHIEVED when the target is reached.

### 4. Visualize Income vs. Expenses Over Time
Open the Monthly Trend view to see a month-by-month breakdown of total income, total expenses,
and net balance — both as a table and as an interactive bar chart. Adjust the date range to
focus on any period of interest.

### 5. Organize with Custom Categories
The system ships with 12 default categories. If your situation requires more granularity
(e.g., "Gym", "Subscriptions", "Pet Care"), create custom categories in the Categories view
and use them immediately across transactions and budgets.

### 6. Manage Your Account
Update your display name, change your password, or delete your account entirely from the
Profile view. All data associated with your account is removed on deletion.

---

## Key Design Decisions

| Decision | Rationale |
|---|---|
| No frontend framework | Eliminates build tools, bundlers, and npm entirely. The UI is a single HTML file served directly by Spring Boot — zero deployment complexity. |
| Same-origin frontend | Placing static files in `src/main/resources/static/` means the browser and the API share the same origin. No CORS configuration needed. |
| JWT in localStorage | Persists across browser tabs and sessions. Expiry is handled reactively — any 401 response triggers an automatic logout and redirect to the login page. |
| Liquibase migrations | Schema changes are versioned, repeatable, and auditable. Default categories are seeded via migration, not application code. |
| BigDecimal everywhere | All monetary values use `java.math.BigDecimal` with `HALF_UP` rounding to avoid floating-point errors in financial calculations. |
| Docker multi-stage build | The final image contains only the JRE and the compiled JAR — no Maven, no JDK, no source code. Minimizes image size and attack surface. |
