# Finance Tracker

A personal finance management application built with Spring Boot and vanilla JavaScript. Track income and expenses, set budgets, manage savings goals, and visualize monthly trends — all from a browser with no installation required.

---

## Features

- **Authentication** — Register, log in, and log out with JWT-based stateless auth
- **Dashboard** — Monthly financial summary (income, expenses, net balance) with over-limit budget alerts
- **Transactions** — Full CRUD with filtering by type, category, and date range; paginated list
- **Budgets** — Set per-category spending limits with visual over-limit indicators
- **Goals** — Savings goals with contribution tracking and progress percentage
- **Categories** — Manage custom categories alongside system defaults
- **Monthly Trend** — Bar/line chart (Chart.js) and table of income vs. expenses over time
- **Profile** — Update display name, change password, delete account

---

## Tech Stack

### Backend
| Concern | Choice |
|---|---|
| Framework | Spring Boot 3.2 (Java 17) |
| Security | Spring Security + Auth0 JWT |
| Database | PostgreSQL 15 |
| Migrations | Liquibase |
| ORM | Spring Data JPA / Hibernate |
| Mapping | MapStruct |
| API Docs | SpringDoc OpenAPI (Swagger UI) |

### Frontend
| Concern | Choice |
|---|---|
| Markup | Plain HTML5 (single `index.html`) |
| Styling | Pico.css 2 via CDN |
| Scripting | Vanilla JavaScript ES2020+ |
| Routing | Hash-based (`#login`, `#dashboard`, etc.) |
| Charts | Chart.js via CDN |

---

## Project Structure

```
finance-tracker/
├── src/
│   └── main/
│       ├── java/com/financetracker/
│       │   ├── config/          Spring Security, CORS, OpenAPI config
│       │   ├── controller/      REST controllers
│       │   ├── dto/             Request / response DTOs
│       │   ├── exception/       Global error handling
│       │   ├── mapper/          MapStruct mappers
│       │   ├── model/           JPA entities
│       │   ├── repository/      Spring Data repositories
│       │   ├── security/        JWT filter and utilities
│       │   └── service/         Business logic
│       └── resources/
│           ├── application.yml          Base config
│           ├── application-dev.yml      Dev profile (env var driven)
│           ├── application-prod.yml     Prod profile (env var driven)
│           ├── db/changelog/            Liquibase migration scripts
│           └── static/                  Frontend (served by Spring Boot)
│               ├── index.html
│               ├── css/styles.css
│               └── js/
│                   ├── api.js           apiFetch wrapper, helpers
│                   ├── auth.js          JWT token helpers
│                   ├── router.js        Hash-based router
│                   ├── dashboard.js
│                   ├── transactions.js
│                   ├── budgets.js
│                   ├── goals.js
│                   ├── categories.js
│                   ├── trend.js
│                   └── profile.js
├── Dockerfile
├── docker-compose.yml
└── .env.example
```

---

## Running with Docker (Recommended)

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running

### Steps

```bash
# 1. Clone the repo and enter the project directory
cd finance-tracker

# 2. Create your environment file
cp .env.example .env

# 3. Edit .env and set the required secrets (see Environment Variables below)

# 4. Build and start everything
docker compose up --build
```

The app will be available at **http://localhost:8080**

To stop:
```bash
docker compose down
```

To stop and remove all data (including the database volume):
```bash
docker compose down -v
```

---

## Running Locally (Development)

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL 15 running locally

### Steps

```bash
# 1. Create a PostgreSQL database
createdb finance_tracker

# 2. Export environment variables
export DB_URL=jdbc:postgresql://localhost:5432/finance_tracker
export DB_USERNAME=your_pg_user
export DB_PASSWORD=your_pg_password
export APP_JWT_SECRET=your-secret-key-at-least-32-chars

# 3. Run with the dev profile
cd finance-tracker
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## Environment Variables

| Variable | Required | Default | Description |
|---|---|---|---|
| `POSTGRES_DB` | No | `finance_tracker` | PostgreSQL database name |
| `POSTGRES_USER` | No | `finance_user` | PostgreSQL username |
| `POSTGRES_PASSWORD` | **Yes** | — | PostgreSQL password |
| `APP_JWT_SECRET` | **Yes** | — | JWT signing secret (min 32 chars) |
| `APP_JWT_EXPIRY_MS` | No | `7200000` | JWT expiry in milliseconds (default 2 hours) |
| `APP_CORS_ALLOWED_ORIGINS` | No | `http://localhost:8080` | Comma-separated allowed CORS origins |
| `HOST_PORT` | No | `8080` | Host port mapped to the app container |

---

## API Documentation

Once the app is running, Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec:
```
http://localhost:8080/v3/api-docs
```

---

## API Overview

| Resource | Endpoint |
|---|---|
| Auth | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |
| Users | `GET/PUT /api/v1/users/me`, `PUT /api/v1/users/me/password`, `DELETE /api/v1/users/me` |
| Transactions | `GET/POST /api/v1/transactions`, `GET/PUT/DELETE /api/v1/transactions/{id}` |
| Budgets | `GET/POST /api/v1/budgets`, `GET/PUT/DELETE /api/v1/budgets/{id}` |
| Goals | `GET/POST /api/v1/goals`, `GET/PUT/DELETE /api/v1/goals/{id}`, `POST /api/v1/goals/{id}/contributions` |
| Categories | `GET/POST /api/v1/categories`, `DELETE /api/v1/categories/{id}` |
| Reports | `GET /api/v1/reports/summary`, `GET /api/v1/reports/monthly-trend` |

All protected endpoints require:
```
Authorization: Bearer <jwt_token>
```

---

## Default Categories

The following categories are seeded automatically on first run and cannot be deleted:

`Food & Dining`, `Transportation`, `Housing`, `Healthcare`, `Entertainment`, `Shopping`, `Education`, `Travel`, `Utilities`, `Income`, `Savings`, `Other`

---

## License

MIT
