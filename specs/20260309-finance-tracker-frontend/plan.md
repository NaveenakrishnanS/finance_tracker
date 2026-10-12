# Finance Tracker Frontend — Implementation Plan

**Specification**: [spec.md](spec.md)
**Backend Spec Reference**: [../20260309-finance-tracker-api/spec.md](../20260309-finance-tracker-api/spec.md)
**Status**: Ready for Review
**Estimated Complexity**: Medium
**Created**: 2026-03-09

---

## Overview

This plan details the implementation of a plain HTML/CSS/vanilla JavaScript single-page
frontend for the Finance Tracker Spring Boot REST API. The frontend is served as static
content from `finance-tracker/src/main/resources/static/`, sharing the same origin as the
API and requiring no CORS configuration. All JavaScript runs directly in modern browsers
with no build step, bundler, or transpiler.

---

## Technical Context

| Concern | Choice |
|---|---|
| Markup | Plain HTML5 — single `index.html` |
| Styling | Pico.css 2.x via jsDelivr CDN (classless baseline) + `css/app.css` for project overrides |
| Scripting | Vanilla JavaScript ES2020+, no frameworks |
| Charting | Chart.js via jsDelivr CDN (trend view only) |
| Routing | Hash-based (`hashchange` event listener in `router.js`) |
| Auth storage | `localStorage` key `ft_token` |
| API base | Empty string `''` (same-origin; change to `http://localhost:8080` only if running frontend from a separate origin) |
| Deployment | Spring Boot `src/main/resources/static/` — served automatically as static content |
| Browser targets | Chrome, Firefox, Edge — current stable versions |

---

## Foundation Compliance

The foundation (`directive/foundation.md` v1.0.0) governs the backend. This frontend must
not violate the following principles from that document that have client-side implications:

| Foundation Principle | Frontend Obligation |
|---|---|
| Security by Default (Principle 5) | JWT attached to every protected request via `apiFetch()`; never logged to console; 401 clears token and redirects |
| Explicit API Contracts (Principle 4) | Frontend consumes the defined contract exactly — field names, status codes, error schema — without assumptions |
| Financial Data Integrity (Principle 1) | `parseFloat()` for amounts from API; `toFixed(2)` for display; `$` prefix hardcoded |
| No server-side logic introduced | Frontend is entirely static; no backend changes permitted during this work |

---

## File Structure

All files reside under `finance-tracker/src/main/resources/static/`.

```
static/
├── index.html              Single-page entry point; all view <section> elements live here
├── css/
│   └── app.css             Project-specific overrides on top of Pico.css
└── js/
    ├── api.js              API_BASE constant, apiFetch() wrapper, 401 global handler
    ├── auth.js             getToken(), setToken(), clearToken(), redirectIfUnauth()
    ├── router.js           hashchange listener, view show/hide, nav bar toggle logic
    ├── dashboard.js        Summary report + over-limit budgets widget
    ├── transactions.js     Paginated list, filters, add/edit/delete form
    ├── budgets.js          Budget list, add/edit/delete form, over-limit indicator
    ├── goals.js            Goal list, add/edit/delete form, contribution sub-form
    ├── categories.js       Category list, add form, system-default guard
    ├── trend.js            Monthly trend table + Chart.js bar/line chart
    └── profile.js          Display name form, change-password form, delete account
```

The three static asset requests on initial page load are: `index.html`, `css/app.css`,
and the browser will load JS files referenced by `<script type="module">` tags. CDN
requests (Pico.css, Chart.js) do not count against the three-request local asset budget
defined in NFR-040.

---

## CDN References

Place these in the `<head>` and at the end of `<body>` of `index.html` respectively:

```html
<!-- Pico.css — classless baseline (in <head>) -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css">

<!-- Chart.js — trend chart (before closing </body>) -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
```

All project JS files are loaded as ES modules:

```html
<script type="module" src="js/router.js"></script>
```

`router.js` imports the view modules as needed; `api.js` and `auth.js` are imported by
every view module.

---

## Implementation Phases

### Phase 1 — Application Shell and Infrastructure

**Objective**: Establish the full file structure, `index.html` skeleton, routing engine,
authentication utilities, and the centralized API fetch wrapper. No view logic yet — only
the scaffolding that every view depends on.

**Deliverable**: A loadable `index.html` that routes between empty view sections, with a
working nav bar on authenticated views and redirection to `#login` for unauthenticated
access.

#### Task 1.1 — Create directory structure

Create all directories and empty placeholder files:

```
static/
  index.html
  css/app.css
  js/api.js
  js/auth.js
  js/router.js
  js/dashboard.js
  js/transactions.js
  js/budgets.js
  js/goals.js
  js/categories.js
  js/trend.js
  js/profile.js
```

#### Task 1.2 — Write `index.html` skeleton

Requirements:
- `<!DOCTYPE html>` with `lang="en"` and a descriptive `<title>` ("Finance Tracker").
- `<head>` includes Pico.css CDN link and `css/app.css`.
- `<body>` contains:
  - A `<nav id="app-nav">` element (hidden by default; shown only on authenticated views).
    Nav links: Dashboard, Transactions, Budgets, Goals, Categories, Trend, Profile, and a
    Log Out button.
  - One `<section>` per view, each with an `id` matching its hash name (e.g.,
    `id="view-login"`, `id="view-dashboard"`, etc.). All sections are hidden by default
    via `display: none` in `app.css` with the class `view`; the router adds an `active`
    class to show the current view.
  - Hash list: `login`, `register`, `dashboard`, `transactions`, `budgets`, `goals`,
    `categories`, `trend`, `profile`.
  - Chart.js CDN `<script>` before the closing `</body>`.
  - `<script type="module" src="js/router.js"></script>` as the last script tag.

The inner HTML of each `<section>` is left empty at this stage — view modules will
populate it dynamically or it will be pre-written in Phase 2 and later phases.

#### Task 1.3 — Implement `js/auth.js`

Export the following named functions:

| Function | Behaviour |
|---|---|
| `getToken()` | Returns `localStorage.getItem('ft_token')` or `null` |
| `setToken(token)` | Calls `localStorage.setItem('ft_token', token)` |
| `clearToken()` | Calls `localStorage.removeItem('ft_token')` |
| `redirectIfUnauth()` | If `getToken()` is null, sets `location.hash = '#login'` and returns `true`; otherwise returns `false` |
| `redirectIfAuth()` | If `getToken()` is not null, sets `location.hash = '#dashboard'` and returns `true`; otherwise returns `false` |

No other logic belongs in this file.

#### Task 1.4 — Implement `js/api.js`

Export:

```js
export const API_BASE = '';  // Same-origin default

export async function apiFetch(path, options = {}) { ... }
```

`apiFetch()` contract:
1. Constructs full URL as `API_BASE + path`.
2. Merges provided `options` with defaults: adds `Authorization: Bearer <token>` header if
   `getToken()` is non-null; sets `Content-Type: application/json` when `options.body` is
   present.
3. Calls `fetch()` and awaits the response.
4. If response status is `401`: calls `clearToken()`, sets `location.hash = '#login'`, and
   throws an error (to abort the calling code).
5. If response is not OK (status >= 400, excluding 401 already handled): attempts to parse
   the body as JSON and throws an object `{ message, fieldErrors }` extracted from the
   error schema `{ timestamp, status, error, message, path, fieldErrors? }`. If JSON
   parsing fails, throws a generic `{ message: 'An unexpected error occurred.' }`.
6. If response is OK (2xx) with a non-empty body, parses and returns the JSON. If the body
   is empty (e.g., `204 No Content`), returns `null`.

Do NOT log the JWT or any password value to the console.

#### Task 1.5 — Implement `js/router.js`

The router is the entry point imported by `index.html`. It:

1. Imports all view modules' `init(container)` functions.
2. Defines a map of hash names to `{ requiresAuth: boolean, init: Function }`.
3. Listens to `hashchange` and fires on `DOMContentLoaded`.
4. On each navigation:
   a. Hides all `<section class="view">` elements.
   b. Reads `location.hash` (defaulting to `#login` if empty or unknown).
   c. Checks `requiresAuth`; if true and no token, redirects to `#login`.
   d. Checks if the target is `#login` or `#register`; if token exists, redirects to
      `#dashboard`.
   e. Shows the nav bar only on authenticated views.
   f. Calls the view's `init()` function, passing the target `<section>` element.
5. Wires the nav bar "Log Out" button: `clearToken()` then `location.hash = '#login'`.

Hash-to-view mapping:

| Hash | Requires Auth | View Module |
|---|---|---|
| `#login` | No | `dashboard.js` registers no init; login view uses inline handlers |
| `#register` | No | — |
| `#dashboard` | Yes | `dashboard.js` |
| `#transactions` | Yes | `transactions.js` |
| `#budgets` | Yes | `budgets.js` |
| `#goals` | Yes | `goals.js` |
| `#categories` | Yes | `categories.js` |
| `#trend` | Yes | `trend.js` |
| `#profile` | Yes | `profile.js` |

The login and register views use pre-written HTML inside their `<section>` elements (added
in Phase 2) with `id`-based event listeners attached by the router on first navigation or
by a dedicated `initLogin()` / `initRegister()` function.

#### Task 1.6 — Write `css/app.css` baseline

Minimal rules required at this stage:

```css
/* Hide all views by default; router adds .active to show */
.view { display: none; }
.view.active { display: block; }

/* Nav bar hidden until authenticated */
#app-nav.hidden { display: none; }
```

Additional rules for indicators (over-limit budgets, achieved goals, error messages) will
be added in the corresponding phase.

**Phase 1 Gate**: Navigate manually to `#login` — the login section becomes visible and
the nav bar is hidden. Navigate to `#dashboard` without a token — redirected to `#login`.
No console errors.

---

### Phase 2 — Authentication Views

**Objective**: Implement login and registration forms with client-side validation, API
integration, auto-login after registration, and post-login redirection.

**Deliverable**: A user can register and be auto-logged-in, or log in directly, and land
on the dashboard hash. A failed login shows an inline error.

#### Task 2.1 — Login view HTML (inside `#view-login` section in `index.html`)

```html
<section id="view-login" class="view">
  <article>
    <h2>Sign In</h2>
    <form id="login-form">
      <label for="login-email">Email</label>
      <input type="email" id="login-email" name="email" required>
      <label for="login-password">Password</label>
      <input type="password" id="login-password" name="password" required>
      <div id="login-error" class="error-msg" hidden></div>
      <button type="submit" id="login-btn">Sign In</button>
    </form>
    <p>No account? <a href="#register">Register</a></p>
  </article>
</section>
```

#### Task 2.2 — Register view HTML (inside `#view-register` section in `index.html`)

```html
<section id="view-register" class="view">
  <article>
    <h2>Create Account</h2>
    <form id="register-form">
      <label for="reg-email">Email</label>
      <input type="email" id="reg-email" name="email" required>
      <label for="reg-password">Password</label>
      <input type="password" id="reg-password" name="password" required>
      <label for="reg-name">Display Name (optional)</label>
      <input type="text" id="reg-name" name="displayName">
      <div id="reg-error" class="error-msg" hidden></div>
      <button type="submit" id="reg-btn">Create Account</button>
    </form>
    <p>Already registered? <a href="#login">Sign In</a></p>
  </article>
</section>
```

#### Task 2.3 — Login form handler

Wire inside `router.js` (or a dedicated `js/login.js` imported by the router) when
`#login` is navigated to:

1. Attach a `submit` listener to `#login-form` (guard against double-attach with a flag
   or `{ once: false }` and `removeEventListener` pattern).
2. On submit:
   a. Prevent default.
   b. Read email and password values.
   c. Client-side validation: email must be non-empty and contain `@`; password must be
      non-empty. Show inline error in `#login-error` if either check fails; return early.
   d. Disable `#login-btn` and set its text to `"Loading..."`.
   e. Call `apiFetch('/api/v1/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })`.
   f. On success: call `setToken(data.token)`, set `location.hash = '#dashboard'`.
   g. On error: display `error.message` in `#login-error`, re-enable button with original
      label.

#### Task 2.4 — Register form handler

Wire when `#register` is navigated to:

1. On submit:
   a. Prevent default.
   b. Client-side validation (same email/password rules as login).
   c. Disable button, set to `"Loading..."`.
   d. POST to `/api/v1/auth/register` with `{ email, password, displayName }`.
   e. On `201` success (register returns no token): immediately POST to
      `/api/v1/auth/login` with the same `{ email, password }`.
   f. On login success: `setToken(data.token)`, redirect to `#dashboard`.
   g. On any error: display error message inline, re-enable button.

#### Task 2.5 — Error message styling in `css/app.css`

```css
.error-msg {
  color: var(--pico-color-red-500, #e53e3e);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}
.success-msg {
  color: var(--pico-color-green-500, #38a169);
  font-size: 0.875rem;
  margin-top: 0.25rem;
}
```

**Phase 2 Gate**: Register a new user — lands on `#dashboard`. Log out and log back in.
Enter wrong password — error message appears inline, no page reload, button re-enables.
Navigate to `#login` while already authenticated — redirected to `#dashboard`.

---

### Phase 3 — Dashboard View

**Objective**: Implement the dashboard with the financial summary report, date range
controls, and the over-limit budgets widget.

**Deliverable**: The dashboard displays total income, total expenses, net balance, and a
per-category breakdown table. Over-limit budgets are listed with a link to the budgets
view. Date range re-fetches the summary.

#### Task 3.1 — Dashboard view HTML

Place inside `#view-dashboard` section:

```html
<h2>Dashboard</h2>
<section>
  <form id="summary-filter-form">
    <label for="summary-start">From</label>
    <input type="date" id="summary-start">
    <label for="summary-end">To</label>
    <input type="date" id="summary-end">
    <button type="submit">Apply</button>
  </form>
  <div id="summary-cards">
    <!-- Populated by dashboard.js -->
  </div>
  <div id="category-breakdown">
    <!-- Populated by dashboard.js -->
  </div>
</section>
<section id="over-limit-widget">
  <h3>Over-Limit Budgets</h3>
  <div id="over-limit-list"><!-- Populated by dashboard.js --></div>
</section>
```

#### Task 3.2 — Implement `js/dashboard.js`

Export `init(container)`:

1. Call `redirectIfUnauth()`; return if unauthenticated.
2. Call `loadSummary()` with no date arguments (defaults to current month on server).
3. Call `loadOverLimitBudgets()`.
4. Wire the `#summary-filter-form` submit event to call `loadSummary(startDate, endDate)`.

`loadSummary(startDate, endDate)`:
- Build query string: append `startDate` and `endDate` only if non-empty.
- `apiFetch('/api/v1/reports/summary?' + params)`.
- On success, populate `#summary-cards` with three summary cards:
  - Total Income: `formatMoney(data.totalIncome)`
  - Total Expenses: `formatMoney(data.totalExpenses)`
  - Net Balance: `formatMoney(data.netBalance)`
- Populate `#category-breakdown` with a `<table>` of per-category rows
  (`data.categoryBreakdown` array with `categoryName`, `totalIncome`, `totalExpenses`).
- On error, show error message in `#summary-cards`.

`loadOverLimitBudgets()`:
- `apiFetch('/api/v1/budgets')`.
- Filter response array for items where `budget.overLimit === true`.
- If none: show "No budgets are currently over their limit." in `#over-limit-list`.
- If some: render a list with category name, limit, spent, and a link `<a href="#budgets">
  View Budgets</a>`.

#### Task 3.3 — Shared `formatMoney()` and `formatDate()` helpers

Add to `js/api.js` (exported) or a new `js/utils.js` imported by all view modules:

```js
export function formatMoney(value) {
  return '$' + parseFloat(value).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

export function formatDate(isoDate) {
  // isoDate is YYYY-MM-DD; new Date() may interpret as UTC midnight
  const [year, month, day] = isoDate.split('-').map(Number);
  return new Date(year, month - 1, day).toLocaleDateString();
}
```

Note: `new Date(isoDate)` treats `YYYY-MM-DD` as UTC, which can cause off-by-one errors
on locale display. The explicit `new Date(year, month-1, day)` constructor uses local time
and avoids this issue.

**Phase 3 Gate**: Dashboard loads and shows current-month summary. Apply a custom date
range and verify numbers update. Confirm over-limit widget shows "none" or correct budgets.

---

### Phase 4 — Categories View

**Objective**: Implement categories before transactions and budgets because categories are
a dependency of both forms.

**Deliverable**: Categories list renders system-default and custom categories. Custom
categories can be added and deleted. System defaults have no delete button. A `409` on
delete shows a readable error.

#### Task 4.1 — Categories view HTML

```html
<h2>Categories</h2>
<section>
  <form id="category-form">
    <label for="cat-name">Category Name</label>
    <input type="text" id="cat-name" required>
    <div id="cat-error" class="error-msg" hidden></div>
    <button type="submit" id="cat-btn">Add Category</button>
  </form>
</section>
<section>
  <div id="cat-list"><!-- Populated by categories.js --></div>
</section>
```

#### Task 4.2 — Implement `js/categories.js`

Export `init(container)`:

1. `redirectIfUnauth()`.
2. Call `loadCategories()`.
3. Wire `#category-form` submit.

Module-level cache variable:

```js
let categoriesCache = null;
```

`loadCategories()`:
- If `categoriesCache` is not null, render from cache and return (NFR-041).
- `apiFetch('/api/v1/categories')`.
- Store result in `categoriesCache`.
- Render `#cat-list` as a `<table>` with columns: Name, Type (System / Custom), Actions.
  - System-default rows: no delete button.
  - Custom rows: a "Delete" button that calls `deleteCategory(id)`.

`deleteCategory(id)`:
- `confirm("Delete this category?")` — return if cancelled.
- `apiFetch('/api/v1/categories/' + id, { method: 'DELETE' })`.
- On success: invalidate cache (`categoriesCache = null`) and call `loadCategories()`.
- On error: if the caught error contains `status: 409` or message indicating conflict,
  display "This category is in use by existing transactions or budgets and cannot be
  deleted." in `#cat-error`. Otherwise show the generic error message.

Add category submit:
- Validate `#cat-name` is non-empty.
- Disable button, set "Loading...".
- `apiFetch('/api/v1/categories', { method: 'POST', body: JSON.stringify({ name }) })`.
- On success: clear input, invalidate cache, call `loadCategories()`.
- On error: show error message in `#cat-error`.

Export `getCategories()` for use by other view modules:

```js
export async function getCategories() {
  if (!categoriesCache) {
    categoriesCache = await apiFetch('/api/v1/categories');
  }
  return categoriesCache;
}
```

**Phase 4 Gate**: Categories list loads. Add a custom category — appears in list. Attempt
to delete a system default — no button present. Delete a custom category — removed. Delete
a category in use — `409` message shown.

---

### Phase 5 — Transactions View

**Objective**: Implement the paginated, filterable transactions list with full CRUD.

**Deliverable**: Transactions table shows paginated rows with Edit and Delete. Filters
reset pagination. Add and edit forms work. Category dropdown is populated from the
categories API.

#### Task 5.1 — Transactions view HTML

```html
<h2>Transactions</h2>
<section>
  <!-- Add/Edit Form -->
  <details id="tx-form-details">
    <summary id="tx-form-toggle">Add Transaction</summary>
    <form id="tx-form">
      <input type="hidden" id="tx-id">
      <label for="tx-amount">Amount</label>
      <input type="number" id="tx-amount" min="0.01" step="0.01" required>
      <label for="tx-type">Type</label>
      <select id="tx-type">
        <option value="INCOME">Income</option>
        <option value="EXPENSE">Expense</option>
      </select>
      <label for="tx-category">Category</label>
      <select id="tx-category"></select>
      <label for="tx-date">Date</label>
      <input type="date" id="tx-date" required>
      <label for="tx-desc">Description (optional)</label>
      <textarea id="tx-desc" rows="2"></textarea>
      <div id="tx-error" class="error-msg" hidden></div>
      <button type="submit" id="tx-btn">Add Transaction</button>
      <button type="button" id="tx-cancel" hidden>Cancel</button>
    </form>
  </details>
</section>
<section>
  <!-- Filters -->
  <form id="tx-filter-form">
    <select id="filter-type">
      <option value="">All Types</option>
      <option value="INCOME">Income</option>
      <option value="EXPENSE">Expense</option>
    </select>
    <select id="filter-category"><option value="">All Categories</option></select>
    <input type="date" id="filter-start">
    <input type="date" id="filter-end">
    <button type="submit">Apply Filters</button>
    <button type="reset" id="filter-reset">Clear</button>
  </form>
  <div id="tx-list"><!-- Populated by transactions.js --></div>
  <div id="tx-pagination">
    <button id="tx-prev">Previous</button>
    <span id="tx-page-info"></span>
    <button id="tx-next">Next</button>
  </div>
</section>
```

#### Task 5.2 — Implement `js/transactions.js`

State variables (module-level):

```js
let currentPage = 0;
const PAGE_SIZE = 20;
let currentFilters = {};
let currentEditId = null;
```

Export `init(container)`:

1. `redirectIfUnauth()`.
2. `loadCategories()` (from `categories.js`) to populate `#tx-category` and
   `#filter-category` dropdowns.
3. Call `loadTransactions()`.
4. Wire `#tx-form` submit → `handleFormSubmit()`.
5. Wire `#tx-filter-form` submit → reset `currentPage = 0`, read filters, call
   `loadTransactions()`.
6. Wire `#filter-reset` → clear filters, reset page, call `loadTransactions()`.
7. Wire `#tx-prev` / `#tx-next` → decrement/increment `currentPage`, call
   `loadTransactions()`.
8. Wire `#tx-cancel` → `resetForm()`.

`loadTransactions()`:
- Build query: `page=${currentPage}&size=${PAGE_SIZE}` plus any active filters
  (`type`, `categoryId`, `startDate`, `endDate`).
- `apiFetch('/api/v1/transactions?' + params)`.
- Render `#tx-list` as a `<table>` with columns: Date, Type, Category, Amount,
  Description (truncated to 50 chars with ellipsis), Actions (Edit, Delete).
- Update pagination: disable `#tx-prev` if `data.first`, disable `#tx-next` if
  `data.last`. Set `#tx-page-info` to `"Page ${data.number + 1} of ${data.totalPages}"`.
- If `data.content` is empty, show "No transactions found." instead of the table.

`handleFormSubmit()`:
- Client-side validation: amount > 0, type selected, category selected, date non-empty.
- Disable button, set "Loading...".
- If `currentEditId` is null: POST to `/api/v1/transactions`.
- Else: PUT to `/api/v1/transactions/${currentEditId}`.
- On success: `resetForm()`, `loadTransactions()`.
- On error: show `error.message` (and `fieldErrors` if present) in `#tx-error`.

`editTransaction(id)`:
- `apiFetch('/api/v1/transactions/' + id)`.
- Populate form fields with returned data.
- Set `currentEditId = id`.
- Change `#tx-form-toggle` text to "Edit Transaction", `#tx-btn` label to "Update
  Transaction", show `#tx-cancel`.
- Open `<details>` if collapsed.

`deleteTransaction(id)`:
- `confirm("Delete this transaction?")` — return if cancelled.
- `apiFetch('/api/v1/transactions/' + id, { method: 'DELETE' })`.
- On success: `loadTransactions()`.

`resetForm()`:
- Reset `#tx-form`, clear `currentEditId = null`.
- Restore "Add Transaction" labels.
- Hide `#tx-cancel`.

**Phase 5 Gate**: Transactions table loads with pagination. Filters narrow the list.
Add a transaction — appears in list. Edit a transaction — form pre-populates, update
persists. Delete with confirmation — row removed.

---

### Phase 6 — Budgets View

**Objective**: Implement the budgets list with over-limit visual indicator and CRUD form.

**Deliverable**: Budgets render with computed spent/remaining amounts. Over-limit rows are
visually distinguished. Add, edit limit, and delete work. Category dropdown populated.

#### Task 6.1 — Budgets view HTML

```html
<h2>Budgets</h2>
<section>
  <form id="budget-form">
    <input type="hidden" id="budget-id">
    <label for="budget-category">Category</label>
    <select id="budget-category" required></select>
    <label for="budget-limit">Limit Amount</label>
    <input type="number" id="budget-limit" min="0.01" step="0.01" required>
    <label for="budget-month">Month</label>
    <input type="number" id="budget-month" min="1" max="12" required>
    <label for="budget-year">Year</label>
    <input type="number" id="budget-year" min="2000" max="2100" required>
    <div id="budget-error" class="error-msg" hidden></div>
    <button type="submit" id="budget-btn">Add Budget</button>
    <button type="button" id="budget-cancel" hidden>Cancel</button>
  </form>
</section>
<section>
  <div id="budget-list"><!-- Populated by budgets.js --></div>
</section>
```

#### Task 6.2 — Implement `js/budgets.js`

State: `let currentEditId = null;`

Export `init(container)`:

1. `redirectIfUnauth()`.
2. `getCategories()` → populate `#budget-category`.
3. `loadBudgets()`.
4. Wire `#budget-form` submit → `handleBudgetSubmit()`.
5. Wire `#budget-cancel` → `resetBudgetForm()`.

`loadBudgets()`:
- `apiFetch('/api/v1/budgets')`.
- Render as `<table>` with columns: Category, Month/Year, Limit, Spent, Remaining,
  Status, Actions (Edit Limit, Delete).
- Remaining = limit − spent (compute client-side from `limitAmount` and `spentAmount`).
- For each row where `budget.overLimit === true`: add class `over-limit` to the `<tr>`.
- Empty state: "No budgets defined yet."

Over-limit styling in `css/app.css`:

```css
tr.over-limit td { color: var(--pico-color-red-500, #e53e3e); }
tr.over-limit td:last-child { /* keep action buttons readable */ color: inherit; }
.over-limit-badge {
  background: var(--pico-color-red-500, #e53e3e);
  color: white;
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  font-size: 0.75rem;
}
```

`handleBudgetSubmit()`:
- Validate: category, limit > 0, month 1–12, year present.
- If `currentEditId` is null: POST `/api/v1/budgets`.
- Else: PUT `/api/v1/budgets/${currentEditId}` with `{ limitAmount }` only (edit only
  changes the limit).
- On success: `resetBudgetForm()`, `loadBudgets()`.

`editBudget(budget)`:
- Set `currentEditId = budget.id`.
- Pre-fill `#budget-limit` with `budget.limitAmount`.
- Lock category, month, year fields (set `disabled` on them) since only limit is editable.
- Show "Update Budget" label on button, show cancel.

`deleteBudget(id)`:
- Confirm, then DELETE `/api/v1/budgets/${id}`. Reload on success.

**Phase 6 Gate**: Budgets list renders with spent/remaining computed. An over-limit budget
row shows red text and badge. Add budget works. Edit changes only the limit. Delete with
confirm works.

---

### Phase 7 — Goals View

**Objective**: Implement the goals list with progress tracking, CRUD, and the per-goal
contribution form.

**Deliverable**: Goals display progress percentage and status. ACHIEVED goals have a
distinct badge. Contributions update progress. Add, edit, delete work.

#### Task 7.1 — Goals view HTML

```html
<h2>Goals</h2>
<section>
  <form id="goal-form">
    <input type="hidden" id="goal-id">
    <label for="goal-name">Name</label>
    <input type="text" id="goal-name" required>
    <label for="goal-target">Target Amount</label>
    <input type="number" id="goal-target" min="0.01" step="0.01" required>
    <label for="goal-date">Target Date</label>
    <input type="date" id="goal-date" required>
    <div id="goal-error" class="error-msg" hidden></div>
    <button type="submit" id="goal-btn">Add Goal</button>
    <button type="button" id="goal-cancel" hidden>Cancel</button>
  </form>
</section>
<section>
  <div id="goal-list"><!-- Populated by goals.js --></div>
</section>
```

Each goal card/row rendered dynamically includes an inline contribution sub-form:

```html
<!-- Per-goal contribution form (rendered inside each row by goals.js) -->
<form class="contribution-form" data-goal-id="{{id}}">
  <input type="number" class="contrib-amount" min="0.01" step="0.01" placeholder="Amount" required>
  <button type="submit">Add Contribution</button>
  <span class="contrib-error error-msg" hidden></span>
</form>
```

#### Task 7.2 — Implement `js/goals.js`

State: `let currentEditId = null;`

Export `init(container)`:

1. `redirectIfUnauth()`.
2. `loadGoals()`.
3. Wire `#goal-form` submit → `handleGoalSubmit()`.
4. Wire `#goal-cancel` → `resetGoalForm()`.
5. Use event delegation on `#goal-list` for Edit, Delete, and contribution form submits.

`loadGoals()`:
- `apiFetch('/api/v1/goals')`.
- Render goals as cards or table rows with: Name, Target, Contributed, Remaining
  (`targetAmount - contributedAmount`), Progress (`progressPercentage + '%'`), Target
  Date (`formatDate`), Status badge, and a contribution form.
- For `ACHIEVED` goals: add `achieved` class to the row/card.
- Empty state: "No goals defined yet."

ACHIEVED styling in `css/app.css`:

```css
.achieved-badge {
  background: var(--pico-color-green-500, #38a169);
  color: white;
  padding: 0.1rem 0.4rem;
  border-radius: 4px;
  font-size: 0.75rem;
}
```

`handleContribution(goalId, amount)`:
- `apiFetch('/api/v1/goals/' + goalId + '/contributions', { method: 'POST', body: JSON.stringify({ amount }) })`.
- On success: `loadGoals()` to refresh progress.
- On error: show error near that goal's contribution form.

`handleGoalSubmit()`:
- Validate name non-empty, target > 0, date present.
- POST or PUT depending on `currentEditId`.
- On success: `resetGoalForm()`, `loadGoals()`.

`editGoal(goal)`, `deleteGoal(id)`: standard pattern matching transactions and budgets.

**Phase 7 Gate**: Goals list renders. Add a goal. Add a contribution — progress percentage
updates. Reach 100% — status shows ACHIEVED badge. Edit and delete work.

---

### Phase 8 — Monthly Trend View

**Objective**: Implement the trend report with a data table and a Chart.js bar or line
chart, with date range filtering.

**Deliverable**: Trend view shows a table of monthly income/expense/net and a Chart.js
chart. Date range changes update both.

#### Task 8.1 — Trend view HTML

```html
<h2>Monthly Trend</h2>
<section>
  <form id="trend-filter-form">
    <label for="trend-start">From</label>
    <input type="date" id="trend-start">
    <label for="trend-end">To</label>
    <input type="date" id="trend-end">
    <button type="submit">Apply</button>
  </form>
  <div id="trend-error" class="error-msg" hidden></div>
  <canvas id="trendChart" height="120"></canvas>
  <div id="trend-table"><!-- Populated by trend.js --></div>
</section>
```

#### Task 8.2 — Implement `js/trend.js`

Module-level variable: `let trendChartInstance = null;`

Export `init(container)`:

1. `redirectIfUnauth()`.
2. `loadTrend()` with no date arguments.
3. Wire `#trend-filter-form` submit → `loadTrend(startDate, endDate)`.

`loadTrend(startDate, endDate)`:
- Build query string from non-empty date params.
- `apiFetch('/api/v1/reports/monthly-trend?' + params)`.
- On success:
  - Render `#trend-table` as a `<table>` with columns: Month/Year, Income, Expenses,
    Net Balance. Format amounts with `formatMoney()`.
  - Destroy existing chart if any: `if (trendChartInstance) { trendChartInstance.destroy(); trendChartInstance = null; }`
  - Create new `Chart` instance on `#trendChart`:
    - Type: `'bar'` (with datasets for income, expenses, net balance).
    - Labels: month-year strings from response data (e.g., `data.monthlyData[n].monthYear`
      or the combination of `year` and `month` fields depending on API response shape).
    - Datasets: Income (green), Expenses (red), Net Balance (blue).
  - Assign new instance to `trendChartInstance`.
- On error: show in `#trend-error`.

API response shape for monthly trend (from backend spec): the endpoint returns a list of
month-year objects. Map the response data array to Chart.js labels and datasets.

**Phase 8 Gate**: Trend view loads and shows table and chart. Apply date range — both
table and chart update. No canvas reuse errors in the console.

---

### Phase 9 — Profile View

**Objective**: Implement the user profile/account view with display name update, password
change, and account deletion.

**Deliverable**: Profile view shows email and display name. User can update display name,
change password, and delete account (with confirmation).

#### Task 9.1 — Profile view HTML

```html
<h2>My Account</h2>
<section>
  <div id="profile-info"><!-- Populated by profile.js --></div>
</section>
<section>
  <h3>Update Display Name</h3>
  <form id="name-form">
    <label for="display-name">Display Name</label>
    <input type="text" id="display-name">
    <div id="name-msg" hidden></div>
    <button type="submit" id="name-btn">Update</button>
  </form>
</section>
<section>
  <h3>Change Password</h3>
  <form id="password-form">
    <label for="current-password">Current Password</label>
    <input type="password" id="current-password" required>
    <label for="new-password">New Password</label>
    <input type="password" id="new-password" required>
    <div id="password-msg" hidden></div>
    <button type="submit" id="password-btn">Change Password</button>
  </form>
</section>
<section>
  <h3>Delete Account</h3>
  <p>This action is permanent and cannot be undone.</p>
  <button id="delete-account-btn" class="secondary">Delete My Account</button>
  <div id="delete-msg" hidden></div>
</section>
```

#### Task 9.2 — Implement `js/profile.js`

Export `init(container)`:

1. `redirectIfUnauth()`.
2. `loadProfile()`.
3. Wire `#name-form` submit.
4. Wire `#password-form` submit.
5. Wire `#delete-account-btn` click.

`loadProfile()`:
- `apiFetch('/api/v1/users/me')`.
- Render `#profile-info` with email and display name.
- Pre-fill `#display-name` input with current display name.

Name form submit:
- Disable button, "Loading...".
- PUT `/api/v1/users/me` with `{ displayName }`.
- On success: show success message in `#name-msg`, re-enable button.
- On error: show error in `#name-msg`.

Password form submit:
- Validate both fields non-empty.
- Disable button.
- PUT `/api/v1/users/me/password` with `{ currentPassword, newPassword }`.
- On success: clear form, show success message.
- On error: show error message.

Delete account button:
- `confirm("Are you sure you want to permanently delete your account? This cannot be undone.")`.
- If confirmed: DELETE `/api/v1/users/me`. On success: `clearToken()`,
  `location.hash = '#login'`.
- On error: show error in `#delete-msg`.

**Phase 9 Gate**: Profile shows email and display name. Update display name persists. Change
password with wrong current password shows API error. Delete account clears token and
redirects to login.

---

### Phase 10 — Polish, Edge Cases, and Cross-Cutting Concerns

**Objective**: Address all cross-cutting requirements that span multiple views: empty
states, loading indicators, responsive nav bar, category cache invalidation, and final
spec compliance review.

**Deliverable**: All acceptance scenarios pass. No raw JSON visible to users on errors.
No duplicate form submissions possible. Pagination controls correctly disabled on first/last
page. Empty states show meaningful messages.

#### Task 10.1 — Loading state pattern audit

Review all form submit handlers across all view modules. Confirm every handler:
1. Disables the submit button and sets its text to `"Loading..."` before the `apiFetch` call.
2. Restores the button (text and `disabled = false`) in both the success and error paths.

Introduce a helper in `api.js`:

```js
export function setLoading(btn, loading, defaultLabel) {
  btn.disabled = loading;
  btn.textContent = loading ? 'Loading...' : defaultLabel;
}
```

#### Task 10.2 — Empty state messages audit

Confirm all list containers (`#tx-list`, `#budget-list`, `#goal-list`, `#cat-list`,
`#over-limit-list`, `#trend-table`, `#category-breakdown`) display a meaningful "No
[items] found." message when the API returns an empty array or `content: []`.

#### Task 10.3 — Pagination controls audit

In `js/transactions.js` `loadTransactions()`, confirm:
- `#tx-prev` is `disabled` when `data.first === true` (page 0).
- `#tx-next` is `disabled` when `data.last === true` (last page).
- Page info text is shown even when `totalPages === 0` (show "Page 0 of 0" or "No results").

#### Task 10.4 — Error display for `fieldErrors`

In `apiFetch()` error handling and in each form's error display, confirm that when the API
returns a `fieldErrors` array, each field error message is rendered (e.g., in a `<ul>`)
rather than only the top-level `message`. Example:

```js
function renderError(errorEl, err) {
  errorEl.hidden = false;
  if (err.fieldErrors && err.fieldErrors.length) {
    errorEl.innerHTML = '<ul>' +
      err.fieldErrors.map(fe => `<li>${fe.field}: ${fe.message}</li>`).join('') +
      '</ul>';
  } else {
    errorEl.textContent = err.message || 'An unexpected error occurred.';
  }
}
```

Export this helper from a shared location (e.g., `api.js` or `utils.js`).

#### Task 10.5 — Category cache invalidation

After any successful category create or delete in `categories.js`, `categoriesCache` is
set to `null`. This ensures other views that call `getCategories()` after navigating back
will receive fresh data. Document this clearly in a comment in `categories.js`.

Limitation: If the user adds a category, navigates to transactions, and opens the
transaction form without reloading, the old category cache is served. This is acceptable
per NFR-041; the cache lives for the page session and the user can refresh if needed.

#### Task 10.6 — Responsive nav bar review

Verify that Pico.css's default responsive behaviour is sufficient for mobile viewports
(375px width). The nav bar uses a standard `<nav>` with an `<ul>` of links. If links
overflow on small screens, add minimal CSS to allow wrapping:

```css
#app-nav ul {
  flex-wrap: wrap;
  gap: 0.5rem;
}
```

#### Task 10.7 — Final spec compliance checklist

Walk through every acceptance scenario in `spec.md` and confirm each maps to implemented
behaviour:

- Scenarios 1–5: Authentication flows (Phases 1–2).
- Scenarios 6–7: Dashboard summary and date range (Phase 3).
- Scenarios 8–12: Transactions CRUD, pagination, filters (Phase 5).
- Scenarios 13–16: Budgets CRUD, over-limit (Phase 6).
- Scenarios 17–20: Goals CRUD, contributions, ACHIEVED badge (Phase 7).
- Scenarios 21–24: Categories CRUD, system-default guard, 409 handling (Phase 4).
- Scenarios 25–26: Monthly trend table and chart with date range (Phase 8).
- All edge cases: 401 global handler, missing JWT redirect, loading states, pagination
  disable, empty states.

---

## Dependencies and Sequencing

```
Phase 1 (Shell)
    └── Phase 2 (Auth)
            └── Phase 3 (Dashboard)  ←── depends on Phase 1 for apiFetch
            └── Phase 4 (Categories) ←── must precede Phases 5 and 6
                    ├── Phase 5 (Transactions) ←── uses getCategories()
                    └── Phase 6 (Budgets)      ←── uses getCategories()
            └── Phase 7 (Goals)     ←── independent of categories
            └── Phase 8 (Trend)     ←── independent of categories
            └── Phase 9 (Profile)   ←── independent of categories
Phase 10 (Polish) ←── depends on all prior phases
```

Phases 3–9 can be executed in any order after Phase 2 is complete, with the exception that
Phase 4 must be done before Phase 5 and Phase 6 (because transactions and budgets import
`getCategories()`).

---

## Risk Assessment

### Low Risk

**CDN availability**: Pico.css and Chart.js are loaded from jsDelivr. jsDelivr has high
uptime; however, an internet outage during development blocks the CDN styles/chart from
loading. Mitigation: note the CDN URLs so a local copy can be substituted temporarily
if needed.

**`<input type="date">` browser UI differences**: Date picker appearance varies between
browsers (especially Firefox). Functional behaviour is consistent. Mitigation: none
required; spec acknowledges acceptable variance.

### Medium Risk

**Chart.js API response shape for `monthly-trend`**: The spec states the response is a
month-year keyed breakdown but the exact field names (`monthYear` string vs. separate
`year`/`month` integers vs. an array of objects) depend on the backend implementation.
Mitigation: inspect the actual API response on first integration and adapt the label
mapping in `trend.js`. The Chart.js code is isolated to one module.

**`401` global redirect race**: If multiple concurrent `apiFetch()` calls are in flight
and all return `401` simultaneously (e.g., on a dashboard with two API calls), the router
may attempt to navigate to `#login` multiple times. Mitigation: add a module-level flag
in `api.js` (`let redirectingToLogin = false`) that prevents the redirect block from
executing more than once per session.

**Category cache stale after add/delete on other views**: The `categoriesCache` in
`categories.js` is invalidated only when the categories view performs a mutation.
Mutations from other views (none currently) would not invalidate the cache. This is not
a risk for the current scope but should be documented for future feature work.

### Low Risk (Spec Alignment)

**BigDecimal serialization**: The spec and backend confirm `BigDecimal` values are
serialized as JSON numbers. `parseFloat()` handles them correctly. No risk.

**JWT field name**: The spec confirms the login response is `{ token, expiresIn }` and
the frontend accesses `data.token`. Confirmed.

---

## Testing Strategy

### Manual Acceptance Testing (Primary)

Because this is a plain static frontend with no build pipeline, automated browser testing
is out of scope for the initial release. Manual testing covers all acceptance scenarios
from `spec.md`.

**Test environment**: Spring Boot backend running on `http://localhost:8080` with the
frontend served from `src/main/resources/static/`. Open `http://localhost:8080` in
Chrome, Firefox, and Edge.

**Test checklist per phase** (expand each phase gate into a full walkthrough):

| Scenario | Steps | Expected Result |
|---|---|---|
| Register + auto-login | Fill register form, submit | Lands on dashboard, `ft_token` in localStorage |
| Login with bad credentials | Submit wrong password | Inline error, no redirect |
| JWT expiry simulation | Delete `ft_token` from DevTools, make any API call | Redirected to `#login` |
| Dashboard default load | Navigate to `#dashboard` | Shows current-month summary |
| Dashboard custom range | Enter dates, click Apply | Summary figures update |
| Dashboard over-limit widget | Create budget with 0 limit, add expense exceeding it | Budget appears in widget |
| Transactions pagination | Add 25+ transactions | Pagination controls appear; prev disabled on page 1 |
| Transaction filters | Filter by type, category, date | List narrows correctly |
| Add/edit/delete transaction | Full CRUD walkthrough | List refreshes each time |
| Budget over-limit indicator | Spend exceeds limit | Row turns red, badge shown |
| Goal ACHIEVED | Contribute up to target amount | ACHIEVED badge appears |
| Categories 409 | Delete category used by a transaction | Error message "in use" shown |
| System default no-delete | Attempt to delete "Food" | No delete button present |
| Monthly trend chart | Navigate to `#trend` | Table and chart render |
| Trend chart date range | Apply range, apply again | Chart destroys and re-creates without errors |
| Profile update name | Change display name | Success message shown |
| Profile change password | Wrong current password | API error shown |
| Delete account | Confirm deletion | Redirected to login, token cleared |
| Mobile layout | Open at 375px width | Nav wraps, forms usable |

### Console Error Audit

Open browser DevTools console during the full test pass. Zero errors or unhandled promise
rejections are required before the implementation is considered complete.

### Empty State Verification

Verify each view with a fresh account (no data) shows the correct empty state message
rather than a blank section.

---

## Assumptions

1. The Spring Boot backend is running and the frontend is served from the same origin
   (`http://localhost:8080` in development). No CORS configuration is added.
2. The login endpoint returns `{ token, expiresIn }` as confirmed by the backend spec.
   The frontend accesses `response.token`.
3. The monthly-trend endpoint returns an array of objects with `income`, `expenses`, and
   `netBalance` numeric fields, plus some form of month-year identifier. The exact field
   name for the month-year label will be confirmed on first integration with the actual
   backend.
4. The categories endpoint returns an array (not a `Page` object), since the spec does not
   mention pagination for categories. If the backend returns a `Page`, the frontend will
   access `.content` instead.
5. The goals contribution endpoint (`POST /api/v1/goals/{id}/contributions`) accepts
   `{ amount }` in the request body and returns the updated goal object (or at least a
   `200 OK`), sufficient to trigger a list refresh.
6. `PUT /api/v1/users/me/password` returns `204 No Content` per the API spec. The frontend
   will not attempt to parse the response body for this call.
7. Browser targets (Chrome, Firefox, Edge — current stable) all natively support
   `fetch`, `async/await`, `localStorage`, ES modules (`type="module"`), and
   `<input type="date">`.

---

## Out of Scope

- Build tooling, bundling, or transpilation of any kind.
- Automated browser test suite (Playwright, Cypress, etc.).
- Offline support or Progressive Web App (PWA) features.
- Multi-currency display.
- Token refresh without re-login.
- Category rename (no backend endpoint exists).
- Accessibility audit beyond semantic HTML (WCAG compliance not required for initial release).
- Receipt image attachments.
- Goal contribution history listing.

---

## Review & Acceptance Checklist

### Plan Completeness
- [x] All views from the spec's Page/View Inventory are covered (9 views)
- [x] All functional requirements (FR-001 through FR-083) are addressed by a phase and task
- [x] All non-functional requirements (NFR-001 through NFR-041) are addressed
- [x] All acceptance scenarios (1–26) and edge cases are covered
- [x] File structure matches the spec exactly
- [x] CDN references match the spec exactly
- [x] API endpoints match the backend spec contract table
- [x] Error response schema (`{ timestamp, status, error, message, path, fieldErrors? }`)
      is handled in `apiFetch()` and form error renderers
- [x] JWT lifecycle (store on login, clear on 401/logout, check on protected view) is
      specified across `auth.js`, `api.js`, and `router.js`
- [x] Pagination (`data.first`, `data.last`, `data.number`, `data.totalPages`) is specified
      for the transactions view
- [x] `formatMoney()` and `formatDate()` helpers are specified with correct implementation
      notes (local-time date constructor to avoid UTC off-by-one)
- [x] `currentEditId` pattern for dual-mode forms is specified for all CRUD views
- [x] Category cache (`categoriesCache`) and `getCategories()` export are specified
- [x] Chart.js destroy-and-recreate pattern for the trend view is specified
- [x] All clarification markers resolved — no `[NEEDS CLARIFICATION]` markers remain

### Foundation Alignment
- [x] No build tools introduced
- [x] Frontend served from same origin as API — no CORS modification to backend required
- [x] JWT stateless auth model respected
- [x] No server-side logic introduced by the frontend
- [x] Financial amounts formatted correctly (`parseFloat` + `toFixed(2)` + `$` prefix)
