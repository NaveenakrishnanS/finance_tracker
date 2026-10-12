# Finance Tracker — API Documentation (Swagger UI)

## Accessing Swagger UI

Once the application is running, the interactive API documentation is available at:

| URL | Purpose |
|---|---|
| `http://localhost:8080/swagger-ui.html` | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs` | Raw OpenAPI 3.0 JSON spec |
| `http://localhost:8080/v3/api-docs.yaml` | Raw OpenAPI 3.0 YAML spec |

---

## How to Authenticate in Swagger UI

All endpoints except **Register** and **Login** require a JWT bearer token.

**Step 1** — Call the Login endpoint to get a token:
1. Expand `Authentication` → `POST /api/v1/auth/login`
2. Click **Try it out**
3. Enter your credentials in the request body:
   ```json
   {
     "email": "user@example.com",
     "password": "secret1234"
   }
   ```
4. Click **Execute**
5. Copy the `token` value from the response body

**Step 2** — Authorize Swagger UI:
1. Click the **Authorize** button (top right, or the lock icon on any endpoint)
2. In the `bearerAuth` field enter: `Bearer <paste_your_token_here>`
3. Click **Authorize** then **Close**

All subsequent requests in this session will include the `Authorization: Bearer ...` header automatically.

---

## API Tag Groups

The API is organized into the following tag groups in Swagger UI:

| Tag | Base Path | Description |
|---|---|---|
| **Authentication** | `/api/v1/auth` | Register and login — no token required |
| **Users** | `/api/v1/users` | View and update your profile, change password, delete account |
| **Transactions** | `/api/v1/transactions` | Record and manage income and expense transactions |
| **Budgets** | `/api/v1/budgets` | Set and track monthly spending limits per category |
| **Goals** | `/api/v1/goals` | Create savings goals and log contributions |
| **Categories** | `/api/v1/categories` | Manage transaction categories |
| **Reports** | `/api/v1/reports` | Financial summary and monthly trend data |

---

## Endpoint Reference

### Authentication

| Method | Path | Auth | Description |
|---|---|---|---|
| `POST` | `/api/v1/auth/register` | None | Register a new user account |
| `POST` | `/api/v1/auth/login` | None | Login and receive a JWT token |

**Register request body:**
```json
{
  "email": "user@example.com",
  "password": "secret1234",
  "displayName": "Jane Doe"
}
```

**Login response body:**
```json
{
  "token": "eyJhbGci...",
  "expiresIn": 7200000
}
```

---

### Users

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/v1/users/me` | JWT | Get current user profile |
| `PUT` | `/api/v1/users/me` | JWT | Update display name |
| `PUT` | `/api/v1/users/me/password` | JWT | Change password |
| `DELETE` | `/api/v1/users/me` | JWT | Delete account and all associated data |

---

### Transactions

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/v1/transactions` | JWT | List transactions (paginated, filterable) |
| `POST` | `/api/v1/transactions` | JWT | Create a transaction |
| `GET` | `/api/v1/transactions/{id}` | JWT | Get a transaction by ID |
| `PUT` | `/api/v1/transactions/{id}` | JWT | Update a transaction |
| `DELETE` | `/api/v1/transactions/{id}` | JWT | Delete a transaction |

**Query parameters for `GET /api/v1/transactions`:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `type` | `INCOME` \| `EXPENSE` | No | Filter by transaction type |
| `categoryId` | Long | No | Filter by category ID |
| `startDate` | `yyyy-MM-dd` | No | Filter from this date (inclusive) |
| `endDate` | `yyyy-MM-dd` | No | Filter to this date (inclusive) |
| `page` | Integer | No | Page number (0-indexed, default 0) |
| `size` | Integer | No | Page size (default 20, max 25) |

**Create/update request body:**
```json
{
  "amount": 49.99,
  "type": "EXPENSE",
  "categoryId": 3,
  "date": "2026-03-10",
  "description": "Weekly grocery run"
}
```

---

### Budgets

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/v1/budgets` | JWT | List all budgets with spent/remaining amounts |
| `POST` | `/api/v1/budgets` | JWT | Create a budget |
| `GET` | `/api/v1/budgets/{id}` | JWT | Get a budget by ID |
| `PUT` | `/api/v1/budgets/{id}` | JWT | Update a budget's limit |
| `DELETE` | `/api/v1/budgets/{id}` | JWT | Delete a budget |

**Create/update request body:**
```json
{
  "categoryId": 3,
  "limitAmount": 500.00,
  "month": 3,
  "year": 2026
}
```

**Response includes computed fields:**
```json
{
  "id": 1,
  "categoryId": 3,
  "categoryName": "Food & Dining",
  "limitAmount": 500.00,
  "spentAmount": 320.50,
  "remainingAmount": 179.50,
  "overLimit": false,
  "month": 3,
  "year": 2026
}
```

---

### Goals

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/v1/goals` | JWT | List all savings goals with progress |
| `POST` | `/api/v1/goals` | JWT | Create a savings goal |
| `GET` | `/api/v1/goals/{id}` | JWT | Get a goal by ID |
| `PUT` | `/api/v1/goals/{id}` | JWT | Update a goal |
| `DELETE` | `/api/v1/goals/{id}` | JWT | Delete a goal |
| `POST` | `/api/v1/goals/{id}/contributions` | JWT | Add a contribution to a goal |

**Create/update request body:**
```json
{
  "name": "Emergency Fund",
  "targetAmount": 5000.00,
  "targetDate": "2026-12-31"
}
```

**Contribution request body:**
```json
{
  "amount": 250.00
}
```

**Response includes computed fields:**
```json
{
  "id": 1,
  "name": "Emergency Fund",
  "targetAmount": 5000.00,
  "contributedAmount": 1250.00,
  "remainingAmount": 3750.00,
  "progressPercentage": 25.00,
  "targetDate": "2026-12-31",
  "status": "IN_PROGRESS"
}
```

---

### Categories

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/v1/categories` | JWT | List all categories (system + custom) |
| `POST` | `/api/v1/categories` | JWT | Create a custom category |
| `DELETE` | `/api/v1/categories/{id}` | JWT | Delete a custom category |

> **Note:** System-default categories (`isSystemDefault: true`) cannot be deleted. Attempting to delete one returns `403 Forbidden`. Deleting a category in use by transactions or budgets returns `409 Conflict`.

**Create request body:**
```json
{
  "name": "Gym"
}
```

---

### Reports

| Method | Path | Auth | Description |
|---|---|---|---|
| `GET` | `/api/v1/reports/summary` | JWT | Financial summary for a date range |
| `GET` | `/api/v1/reports/monthly-trend` | JWT | Month-by-month income/expense breakdown |

**Query parameters (both endpoints):**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `startDate` | `yyyy-MM-dd` | No | Start of range (defaults to first day of current month) |
| `endDate` | `yyyy-MM-dd` | No | End of range (defaults to last day of current month) |

**Summary response:**
```json
{
  "totalIncome": 4500.00,
  "totalExpenses": 1823.75,
  "netBalance": 2676.25,
  "startDate": "2026-03-01",
  "endDate": "2026-03-31",
  "categoryBreakdown": [
    {
      "categoryId": 3,
      "categoryName": "Food & Dining",
      "totalIncome": 0.00,
      "totalExpenses": 320.50
    }
  ]
}
```

**Monthly trend response:**
```json
[
  {
    "year": 2026,
    "month": 1,
    "totalIncome": 4500.00,
    "totalExpenses": 2100.00,
    "netBalance": 2400.00
  },
  {
    "year": 2026,
    "month": 2,
    "totalIncome": 4500.00,
    "totalExpenses": 1950.00,
    "netBalance": 2550.00
  }
]
```

---

## Error Response Format

All API errors return a consistent JSON structure:

```json
{
  "timestamp": "2026-03-10T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/transactions"
}
```

For validation errors, a `fieldErrors` array is included:

```json
{
  "timestamp": "2026-03-10T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/v1/transactions",
  "fieldErrors": [
    {
      "field": "amount",
      "message": "Amount must be at least 0.01"
    }
  ]
}
```

### HTTP Status Codes

| Code | Meaning |
|---|---|
| `200 OK` | Request succeeded |
| `201 Created` | Resource created successfully |
| `204 No Content` | Delete or update succeeded with no response body |
| `400 Bad Request` | Validation error or malformed request body |
| `401 Unauthorized` | Missing or invalid JWT token |
| `403 Forbidden` | Authenticated but not allowed (e.g., accessing another user's resource, deleting a system category) |
| `404 Not Found` | Resource does not exist or does not belong to the authenticated user |
| `409 Conflict` | Duplicate resource or constraint violation (e.g., email already registered, category in use) |
| `500 Internal Server Error` | Unexpected server error |

---

## Pagination

All list endpoints that return large datasets support pagination via Spring's `Pageable`:

**Request parameters:**
- `page` — 0-indexed page number (default: `0`)
- `size` — records per page (default: `20`, max: `25`)
- `sort` — field and direction, e.g. `date,desc`

**Response shape (Spring `Page` object):**
```json
{
  "content": [...],
  "totalElements": 87,
  "totalPages": 5,
  "number": 0,
  "size": 20,
  "first": true,
  "last": false
}
```

---

## Default System Categories

The following categories are seeded on first run and are available to all users:

| ID | Name |
|---|---|
| 1 | Food & Dining |
| 2 | Transportation |
| 3 | Housing |
| 4 | Healthcare |
| 5 | Entertainment |
| 6 | Shopping |
| 7 | Education |
| 8 | Travel |
| 9 | Utilities |
| 10 | Income |
| 11 | Savings |
| 12 | Other |
