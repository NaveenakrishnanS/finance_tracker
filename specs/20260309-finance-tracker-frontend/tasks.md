# Finance Tracker Frontend — Implementation Tasks

**Plan**: [plan.md](plan.md)
**Spec**: [spec.md](spec.md)
**Status**: Completed (2026-03-10)
**Created**: 2026-03-09
**Total Tasks**: 42
**Parallel Tasks**: 18

---

## Task Categories

| Category | Tasks | Count |
|---|---|---|
| Setup | T001–T006 | 6 |
| Auth Views | T007–T011 | 5 |
| Dashboard | T012–T014 | 3 |
| Categories | T015–T016 | 2 |
| Transactions | T017–T018 | 2 |
| Budgets | T019–T020 | 2 |
| Goals | T021–T022 | 2 |
| Monthly Trend | T023–T024 | 2 |
| Profile | T025–T026 | 2 |
| Polish | T027–T042 | 16 |

---

## Phase 1 — Application Shell and Infrastructure

### [X] T001 — Create static directory structure and placeholder files

**File(s)**:
- `finance-tracker/src/main/resources/static/index.html`
- `finance-tracker/src/main/resources/static/css/styles.css`
- `finance-tracker/src/main/resources/static/js/api.js`
- `finance-tracker/src/main/resources/static/js/auth.js`
- `finance-tracker/src/main/resources/static/js/router.js`
- `finance-tracker/src/main/resources/static/js/dashboard.js`
- `finance-tracker/src/main/resources/static/js/transactions.js`
- `finance-tracker/src/main/resources/static/js/budgets.js`
- `finance-tracker/src/main/resources/static/js/goals.js`
- `finance-tracker/src/main/resources/static/js/categories.js`
- `finance-tracker/src/main/resources/static/js/trend.js`
- `finance-tracker/src/main/resources/static/js/profile.js`

**Steps**:
1. Create all directories: `static/`, `static/css/`, `static/js/`.
2. Create each file listed above as an empty placeholder (comment header only).
3. Verify the directory tree matches the file structure from `plan.md` exactly.

**Acceptance**: All 12 files exist at the paths listed above; no build tool or bundler configuration is introduced.

---

### [X] T002 — Write `index.html` skeleton

**File**: `finance-tracker/src/main/resources/static/index.html`

**Steps**:
1. Add `<!DOCTYPE html>` declaration with `<html lang="en">`.
2. Add `<head>` containing:
   - `<meta charset="UTF-8">` and `<meta name="viewport" content="width=device-width, initial-scale=1">`.
   - `<title>Finance Tracker</title>`.
   - Pico.css CDN link: `<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css">`.
   - Local stylesheet: `<link rel="stylesheet" href="css/styles.css">`.
3. Add `<body>` containing:
   - `<nav id="app-nav" class="hidden">` with a `<ul>` of anchor links to each hash view (Dashboard, Transactions, Budgets, Goals, Categories, Trend, Profile) and a `<button id="logout-btn">Log Out</button>`.
   - Nine `<section class="view">` elements with IDs: `view-login`, `view-register`, `view-dashboard`, `view-transactions`, `view-budgets`, `view-goals`, `view-categories`, `view-trend`, `view-profile`. All sections are empty at this stage; content will be added in T007–T025.
   - Chart.js CDN script before closing `</body>`: `<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>`.
   - Entry-point module script as the final tag: `<script type="module" src="js/router.js"></script>`.

**Acceptance**: Opening `index.html` in a browser shows a blank page with no console errors. Nine empty sections exist in the DOM.

**Depends on**: T001

---

### [X] T003 — Implement `js/auth.js`

**File**: `finance-tracker/src/main/resources/static/js/auth.js`

**Steps**:
1. Export `getToken()` — returns `localStorage.getItem('ft_token')` or `null`.
2. Export `setToken(token)` — calls `localStorage.setItem('ft_token', token)`.
3. Export `clearToken()` — calls `localStorage.removeItem('ft_token')`.
4. Export `redirectIfUnauth()` — if `getToken()` is `null`, sets `location.hash = '#login'` and returns `true`; otherwise returns `false`.
5. Export `redirectIfAuth()` — if `getToken()` is not `null`, sets `location.hash = '#dashboard'` and returns `true`; otherwise returns `false`.
6. Add a comment block at the top clarifying that no logic beyond token storage and redirection belongs in this file.
7. Verify that no JWT value is ever logged to `console`.

**Acceptance**: All five functions are exported; the module contains no business logic beyond the five listed functions.

**Depends on**: T001

---

### [X] T004 — Implement `js/api.js` — `apiFetch()`, `API_BASE`, and shared helpers

**File**: `finance-tracker/src/main/resources/static/js/api.js`

**Steps**:
1. Export `const API_BASE = '';` as the first declaration (same-origin default; comment instructs developer to change to `http://localhost:8080` only when running from a separate origin).
2. Import `getToken` and `clearToken` from `./auth.js`.
3. Add a module-level flag: `let redirectingToLogin = false;` to guard against concurrent 401 redirects.
4. Export `async function apiFetch(path, options = {})` with the following contract:
   - Construct URL: `API_BASE + path`.
   - Merge headers: add `Authorization: Bearer <token>` if `getToken()` is non-null; add `Content-Type: application/json` when `options.body` is present.
   - Await `fetch()`.
   - If status is `401` and `redirectingToLogin` is `false`: set flag to `true`, call `clearToken()`, set `location.hash = '#login'`, and throw an error to abort the caller.
   - If status >= 400 (and not 401): parse body as JSON, throw `{ message, fieldErrors }` extracted from the error schema `{ timestamp, status, error, message, path, fieldErrors? }`. On JSON parse failure throw `{ message: 'An unexpected error occurred.' }`.
   - If status is 2xx with non-empty body: parse and return JSON.
   - If status is `204 No Content` or body is empty: return `null`.
5. Export `function formatMoney(value)` — returns `'$' + parseFloat(value).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')`.
6. Export `function formatDate(isoDate)` — splits `YYYY-MM-DD` into `[year, month, day]` and returns `new Date(year, month - 1, day).toLocaleDateString()` (local-time constructor avoids UTC off-by-one).
7. Export `function setLoading(btn, loading, defaultLabel)` — sets `btn.disabled = loading` and `btn.textContent = loading ? 'Loading...' : defaultLabel`.
8. Export `function renderError(errorEl, err)` — sets `errorEl.hidden = false`; if `err.fieldErrors` is a non-empty array, builds a `<ul>` of `field: message` items; otherwise sets `errorEl.textContent` to `err.message || 'An unexpected error occurred.'`.
9. Verify that no JWT or password value is logged to `console` anywhere in the file.

**Acceptance**: Module exports seven items (`API_BASE`, `apiFetch`, `formatMoney`, `formatDate`, `setLoading`, `renderError`). The `redirectingToLogin` guard prevents duplicate 401 redirects.

**Depends on**: T003

---

### [X] T005 — Implement `js/router.js`

**File**: `finance-tracker/src/main/resources/static/js/router.js`

**Steps**:
1. Import `init` functions from all view modules: `dashboard.js`, `transactions.js`, `budgets.js`, `goals.js`, `categories.js`, `trend.js`, `profile.js`.
2. Import `getToken`, `clearToken` from `./auth.js`.
3. Define the route map — a plain object keyed by hash name, each with `{ requiresAuth: boolean, init: Function | null }`:
   - `login` → `{ requiresAuth: false, init: initLogin }` (defined locally in T009)
   - `register` → `{ requiresAuth: false, init: initRegister }` (defined locally in T010)
   - `dashboard` → `{ requiresAuth: true, init: dashboardInit }`
   - `transactions` → `{ requiresAuth: true, init: transactionsInit }`
   - `budgets` → `{ requiresAuth: true, init: budgetsInit }`
   - `goals` → `{ requiresAuth: true, init: goalsInit }`
   - `categories` → `{ requiresAuth: true, init: categoriesInit }`
   - `trend` → `{ requiresAuth: true, init: trendInit }`
   - `profile` → `{ requiresAuth: true, init: profileInit }`
4. Implement `navigate()`:
   - Hide all `<section class="view">` elements.
   - Read `location.hash` (strip leading `#`); default to `login` if empty or unknown.
   - If `requiresAuth` is true and `getToken()` is null: redirect to `#login` and return.
   - If route is `login` or `register` and `getToken()` is not null: redirect to `#dashboard` and return.
   - Show `#app-nav` (remove `hidden` class) for authenticated routes; hide it for `login` and `register`.
   - Show the target `<section>` by adding class `active`.
   - Call the route's `init()` function with the target section element.
5. Add `window.addEventListener('hashchange', navigate)` and `document.addEventListener('DOMContentLoaded', navigate)`.
6. Wire `#logout-btn` click: call `clearToken()` then `location.hash = '#login'`.

**Acceptance**: Navigating to `#login` shows login section, hides nav. Navigating to `#dashboard` without a token redirects to `#login`. Navigating to `#login` with a valid token redirects to `#dashboard`. No console errors on initial load.

**Depends on**: T002, T003, T004

---

### [X] T006 — Write `css/styles.css` baseline

**File**: `finance-tracker/src/main/resources/static/css/styles.css`

**Steps**:
1. Add view visibility rules:
   ```css
   .view { display: none; }
   .view.active { display: block; }
   ```
2. Add nav visibility rule:
   ```css
   #app-nav.hidden { display: none; }
   ```
3. Add error and success message styles:
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
4. Add over-limit budget row styles:
   ```css
   tr.over-limit td { color: var(--pico-color-red-500, #e53e3e); }
   tr.over-limit td:last-child { color: inherit; }
   .over-limit-badge {
     background: var(--pico-color-red-500, #e53e3e);
     color: white;
     padding: 0.1rem 0.4rem;
     border-radius: 4px;
     font-size: 0.75rem;
   }
   ```
5. Add achieved goal badge style:
   ```css
   .achieved-badge {
     background: var(--pico-color-green-500, #38a169);
     color: white;
     padding: 0.1rem 0.4rem;
     border-radius: 4px;
     font-size: 0.75rem;
   }
   ```
6. Add responsive nav wrap rule:
   ```css
   #app-nav ul {
     flex-wrap: wrap;
     gap: 0.5rem;
   }
   ```
7. Verify no inline styles are present in any JS file except for JavaScript-driven dynamic state changes (e.g., `element.style.display`).

**Acceptance**: All CSS classes defined in this file are referenced by at least one task in the task list. No inline styles appear in any HTML or JS file.

**Depends on**: T001

---

## Phase 2 — Authentication Views

### [X] T007 — Add login view HTML to `index.html`

**File**: `finance-tracker/src/main/resources/static/index.html`

**Steps**:
1. Inside `<section id="view-login" class="view">`, add:
   - An `<article>` containing a `<h2>Sign In</h2>` heading.
   - A `<form id="login-form">` with:
     - Email field: `<label for="login-email">` + `<input type="email" id="login-email" name="email" required>`.
     - Password field: `<label for="login-password">` + `<input type="password" id="login-password" name="password" required>`.
     - Error container: `<div id="login-error" class="error-msg" hidden></div>`.
     - Submit button: `<button type="submit" id="login-btn">Sign In</button>`.
   - A `<p>No account? <a href="#register">Register</a></p>` link below the form.

**Acceptance**: `#view-login` section contains the form with all required `id` attributes matching the plan specification.

**Depends on**: T002

---

### [X] T008 — Add register view HTML to `index.html`

**File**: `finance-tracker/src/main/resources/static/index.html`

**Steps**:
1. Inside `<section id="view-register" class="view">`, add:
   - An `<article>` containing a `<h2>Create Account</h2>` heading.
   - A `<form id="register-form">` with:
     - Email field: `<label for="reg-email">` + `<input type="email" id="reg-email" name="email" required>`.
     - Password field: `<label for="reg-password">` + `<input type="password" id="reg-password" name="password" required>`.
     - Display name field: `<label for="reg-name">Display Name (optional)</label>` + `<input type="text" id="reg-name" name="displayName">`.
     - Error container: `<div id="reg-error" class="error-msg" hidden></div>`.
     - Submit button: `<button type="submit" id="reg-btn">Create Account</button>`.
   - A `<p>Already registered? <a href="#login">Sign In</a></p>` link below the form.

**Acceptance**: `#view-register` section contains the form with all required `id` attributes. Display name field is optional (no `required` attribute).

**Depends on**: T002

---

### [X] T009 — Implement login form handler in `js/router.js`

**File**: `finance-tracker/src/main/resources/static/js/router.js`

**Steps**:
1. Implement `initLogin(container)` function within `router.js` (or extract to a small `js/login.js` module imported by the router):
   - Attach a `submit` listener to `#login-form`, guarding against double-attach with a boolean flag (`loginListenerAttached`).
   - On submit:
     a. Call `event.preventDefault()`.
     b. Read `email` and `password` values from `#login-email` and `#login-password`.
     c. Client-side validation: email must be non-empty and contain `@`; password must be non-empty. On failure, call `renderError(document.getElementById('login-error'), { message: '...' })` and return early.
     d. Call `setLoading(loginBtn, true, 'Sign In')`.
     e. Call `apiFetch('/api/v1/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) })`.
     f. On success: call `setToken(data.token)`, set `location.hash = '#dashboard'`.
     g. On error: call `renderError(loginError, err)`, call `setLoading(loginBtn, false, 'Sign In')`.
2. Register `initLogin` in the router's route map for the `login` hash.

**Acceptance**: Submitting with wrong credentials shows an inline error message. Submitting with correct credentials stores the token and navigates to `#dashboard`. Button is disabled during the request and re-enabled on error.

**Depends on**: T004, T005, T007

---

### [X] T010 — Implement register form handler in `js/router.js`

**File**: `finance-tracker/src/main/resources/static/js/router.js`

**Steps**:
1. Implement `initRegister(container)` within `router.js` (or `js/register.js`):
   - Attach a `submit` listener to `#register-form`, guarding against double-attach.
   - On submit:
     a. `event.preventDefault()`.
     b. Read `email`, `password`, `displayName` from the form fields.
     c. Client-side validation: same email and password rules as login. Show error in `#reg-error` on failure.
     d. `setLoading(regBtn, true, 'Create Account')`.
     e. POST to `/api/v1/auth/register` with `{ email, password, displayName }`.
     f. On `201` success (no token returned): immediately POST to `/api/v1/auth/login` with `{ email, password }` (auto-login).
     g. On login success: `setToken(data.token)`, redirect to `#dashboard`.
     h. On any error (register or auto-login): call `renderError(regError, err)`, `setLoading(regBtn, false, 'Create Account')`.
2. Register `initRegister` in the router's route map for the `register` hash.

**Acceptance**: Registering with valid inputs auto-logs in and lands on `#dashboard`. Failed registration shows an inline error. `ft_token` appears in `localStorage` after success.

**Depends on**: T004, T005, T008

---

### [X] T011 — Phase 2 gate verification

**File**: Manual test — no file changes.

**Steps**:
1. Register a new user → verify landing on `#dashboard` and `ft_token` in `localStorage`.
2. Log out → verify token is removed and `#login` view is shown.
3. Log in with the registered credentials → verify `#dashboard` again.
4. Attempt login with wrong password → verify inline error appears without page reload.
5. Navigate to `#login` while authenticated → verify redirect to `#dashboard`.
6. Navigate to `#register` while authenticated → verify redirect to `#dashboard`.
7. Manually delete `ft_token` from DevTools → navigate to `#dashboard` → verify redirect to `#login`.

**Acceptance**: All seven checks pass with zero console errors.

**Depends on**: T009, T010

---

## Phase 3 — Dashboard View

### [X] T012 — Add dashboard view HTML to `index.html`

**File**: `finance-tracker/src/main/resources/static/index.html`

**Steps**:
1. Inside `<section id="view-dashboard" class="view">`, add:
   - `<h2>Dashboard</h2>`.
   - A summary filter `<section>` containing:
     - `<form id="summary-filter-form">` with `<label for="summary-start">From</label>`, `<input type="date" id="summary-start">`, `<label for="summary-end">To</label>`, `<input type="date" id="summary-end">`, `<button type="submit">Apply</button>`.
     - `<div id="summary-cards"><!-- Populated by dashboard.js --></div>`.
     - `<div id="category-breakdown"><!-- Populated by dashboard.js --></div>`.
   - An over-limit widget `<section id="over-limit-widget">` containing:
     - `<h3>Over-Limit Budgets</h3>`.
     - `<div id="over-limit-list"><!-- Populated by dashboard.js --></div>`.

**Acceptance**: `#view-dashboard` section contains all required `id` attributes as specified.

**Depends on**: T002

---

### [X] T013 [P] — Implement `js/dashboard.js`

**File**: `finance-tracker/src/main/resources/static/js/dashboard.js`

**Steps**:
1. Import `apiFetch`, `formatMoney`, `renderError` from `./api.js`.
2. Import `redirectIfUnauth` from `./auth.js`.
3. Export `init(container)`:
   - Call `redirectIfUnauth()`; return immediately if it returns `true`.
   - Call `loadSummary()` with no arguments.
   - Call `loadOverLimitBudgets()`.
   - Wire `#summary-filter-form` submit event: `event.preventDefault()`, read `#summary-start` and `#summary-end` values, call `loadSummary(startDate, endDate)`.
4. Implement `loadSummary(startDate, endDate)`:
   - Build `URLSearchParams` appending `startDate` and `endDate` only if non-empty strings.
   - Call `apiFetch('/api/v1/reports/summary?' + params)`.
   - On success: populate `#summary-cards` with three `<article>` cards showing `formatMoney(data.totalIncome)`, `formatMoney(data.totalExpenses)`, `formatMoney(data.netBalance)` with descriptive labels.
   - Populate `#category-breakdown` with a `<table>` rendering each item in `data.categoryBreakdown` with columns: Category Name, Income, Expenses.
   - If `data.categoryBreakdown` is empty, show "No category data for this period." in `#category-breakdown`.
   - On error: call `renderError` in `#summary-cards`.
5. Implement `loadOverLimitBudgets()`:
   - Call `apiFetch('/api/v1/budgets')`.
   - Filter the response array for items where `budget.overLimit === true`.
   - If none: set `#over-limit-list` text to "No budgets are currently over their limit."
   - If some: render a `<ul>` where each `<li>` shows the category name, limit, and spent amounts, plus an `<a href="#budgets">View Budgets</a>` link.
   - On error: call `renderError` in `#over-limit-list`.

**Acceptance**: Dashboard renders current-month summary on load. Applying a custom date range updates the figures. Over-limit widget shows correct data or the "none" message.

**Depends on**: T004, T005, T012

---

### [X] T014 [P] — Phase 3 gate verification

**File**: Manual test — no file changes.

**Steps**:
1. Navigate to `#dashboard` → verify total income, total expenses, net balance, and category breakdown table render.
2. Enter a custom date range, click Apply → verify summary figures update without page reload.
3. Verify over-limit widget shows "No budgets are currently over their limit." on a fresh account.
4. Create a budget and exceed its limit via transactions → verify the budget appears in the over-limit widget.

**Acceptance**: All four checks pass with zero console errors.

**Depends on**: T013

---

## Phase 4 — Categories View

### [X] T015 — Add categories view HTML to `index.html` and implement `js/categories.js`

**File(s)**:
- `finance-tracker/src/main/resources/static/index.html`
- `finance-tracker/src/main/resources/static/js/categories.js`

**Steps** — HTML (`index.html`):
1. Inside `<section id="view-categories" class="view">`, add:
   - `<h2>Categories</h2>`.
   - A `<section>` containing:
     - `<form id="category-form">` with `<label for="cat-name">Category Name</label>`, `<input type="text" id="cat-name" required>`, `<div id="cat-error" class="error-msg" hidden></div>`, `<button type="submit" id="cat-btn">Add Category</button>`.
   - A `<section>` containing `<div id="cat-list"><!-- Populated by categories.js --></div>`.

**Steps** — JS (`categories.js`):
1. Import `apiFetch`, `renderError`, `setLoading` from `./api.js`.
2. Import `redirectIfUnauth` from `./auth.js`.
3. Declare module-level cache: `let categoriesCache = null;`.
4. Export `init(container)`:
   - `redirectIfUnauth()` guard.
   - Call `loadCategories()`.
   - Wire `#category-form` submit → call `handleAddCategory()`.
5. Implement `loadCategories()`:
   - If `categoriesCache` is not null, render from cache and return (satisfies NFR-041).
   - `apiFetch('/api/v1/categories')`.
   - Store response in `categoriesCache`.
   - Render `#cat-list` as a `<table>` with columns: Name, Type, Actions.
     - Type column: "System" if `category.isSystemDefault === true`, otherwise "Custom".
     - Actions column: for Custom categories only, a `<button>Delete</button>` wired to `deleteCategory(category.id)`.
   - If array is empty, show "No categories found."
6. Implement `deleteCategory(id)`:
   - Show `confirm("Delete this category?")`. Return if cancelled.
   - `apiFetch('/api/v1/categories/' + id, { method: 'DELETE' })`.
   - On success: set `categoriesCache = null`, call `loadCategories()`.
   - On error: if `err.message` indicates a conflict (or status code is 409), display "This category is in use by existing transactions or budgets and cannot be deleted." in `#cat-error`. Otherwise display generic error.
   - Add a comment: "Cache is invalidated on mutation so that other views calling getCategories() receive fresh data on next navigation."
7. Implement `handleAddCategory()`:
   - Validate `#cat-name` is non-empty.
   - `setLoading(catBtn, true, 'Add Category')`.
   - `apiFetch('/api/v1/categories', { method: 'POST', body: JSON.stringify({ name }) })`.
   - On success: clear input, set `categoriesCache = null`, call `loadCategories()`, `setLoading(catBtn, false, 'Add Category')`.
   - On error: `renderError(catError, err)`, `setLoading(catBtn, false, 'Add Category')`.
8. Export `getCategories()` — returns `categoriesCache` if populated, otherwise fetches, stores, and returns it.

**Acceptance**: Categories list renders on load. Add a custom category → it appears. Attempt delete of a system default → no button present. Delete a custom category → removed. Delete a category in use → friendly error message.

**Depends on**: T004, T005, T006

---

### [X] T016 [P] — Phase 4 gate verification

**File**: Manual test — no file changes.

**Steps**:
1. Navigate to `#categories` → verify system-default categories appear with "System" type and no delete button.
2. Add a custom category → verify it appears with "Custom" type and a Delete button.
3. Delete the custom category → verify it is removed from the list.
4. Add a category, attach it to a transaction, then attempt to delete it → verify the "in use" error message.

**Acceptance**: All four checks pass with zero console errors.

**Depends on**: T015

---

## Phase 5 — Transactions View

### [X] T017 — Add transactions view HTML to `index.html` and implement `js/transactions.js`

**File(s)**:
- `finance-tracker/src/main/resources/static/index.html`
- `finance-tracker/src/main/resources/static/js/transactions.js`

**Steps** — HTML (`index.html`):
1. Inside `<section id="view-transactions" class="view">`, add:
   - `<h2>Transactions</h2>`.
   - A `<section>` containing a `<details id="tx-form-details">` with `<summary id="tx-form-toggle">Add Transaction</summary>` and a `<form id="tx-form">` with:
     - `<input type="hidden" id="tx-id">`.
     - Amount: `<label for="tx-amount">Amount</label>` + `<input type="number" id="tx-amount" min="0.01" step="0.01" required>`.
     - Type: `<label for="tx-type">Type</label>` + `<select id="tx-type">` with `INCOME` and `EXPENSE` options.
     - Category: `<label for="tx-category">Category</label>` + `<select id="tx-category"></select>`.
     - Date: `<label for="tx-date">Date</label>` + `<input type="date" id="tx-date" required>`.
     - Description: `<label for="tx-desc">Description (optional)</label>` + `<textarea id="tx-desc" rows="2"></textarea>`.
     - Error: `<div id="tx-error" class="error-msg" hidden></div>`.
     - Buttons: `<button type="submit" id="tx-btn">Add Transaction</button>` + `<button type="button" id="tx-cancel" hidden>Cancel</button>`.
   - A `<section>` containing:
     - Filter `<form id="tx-filter-form">` with type select, category select (`<select id="filter-category"><option value="">All Categories</option></select>`), start date, end date, Apply button, and Clear button (`<button type="reset" id="filter-reset">Clear</button>`).
     - `<div id="tx-list"><!-- Populated by transactions.js --></div>`.
     - Pagination `<div id="tx-pagination">` with `<button id="tx-prev">Previous</button>`, `<span id="tx-page-info"></span>`, `<button id="tx-next">Next</button>`.

**Steps** — JS (`transactions.js`):
1. Import `apiFetch`, `formatMoney`, `formatDate`, `renderError`, `setLoading` from `./api.js`.
2. Import `redirectIfUnauth` from `./auth.js`.
3. Import `getCategories` from `./categories.js`.
4. Declare module-level state: `let currentPage = 0; const PAGE_SIZE = 20; let currentFilters = {}; let currentEditId = null;`.
5. Export `init(container)`:
   - `redirectIfUnauth()` guard.
   - Call `getCategories()` and populate both `#tx-category` and `#filter-category` dropdowns (add an "All Categories" option as the first item in `#filter-category`).
   - Call `loadTransactions()`.
   - Wire event listeners: `#tx-form` submit → `handleFormSubmit()`; `#tx-filter-form` submit → reset `currentPage = 0`, extract filter values into `currentFilters`, call `loadTransactions()`; `#filter-reset` → clear `currentFilters = {}`, `currentPage = 0`, call `loadTransactions()`; `#tx-prev` → `currentPage--`, `loadTransactions()`; `#tx-next` → `currentPage++`, `loadTransactions()`; `#tx-cancel` → `resetForm()`.
6. Implement `loadTransactions()`:
   - Build `URLSearchParams` with `page`, `size`, and any active `currentFilters` fields.
   - `apiFetch('/api/v1/transactions?' + params)`.
   - Render `#tx-list` as a `<table>` with columns: Date, Type, Category, Amount, Description (truncated to 50 chars + `…` if longer), Actions (Edit, Delete buttons).
   - Update pagination: disable `#tx-prev` when `data.first === true`; disable `#tx-next` when `data.last === true`; set `#tx-page-info` text to `"Page ${data.number + 1} of ${data.totalPages}"`.
   - Empty state: if `data.content.length === 0`, show "No transactions found." and hide pagination.
   - On error: `renderError` in `#tx-list`.
7. Implement `handleFormSubmit()`:
   - Validate: amount > 0, type selected, category selected, date non-empty.
   - `setLoading(txBtn, true, currentEditId ? 'Update Transaction' : 'Add Transaction')`.
   - If `currentEditId` is null: `apiFetch('/api/v1/transactions', { method: 'POST', body })`.
   - Else: `apiFetch('/api/v1/transactions/' + currentEditId, { method: 'PUT', body })`.
   - On success: `resetForm()`, `loadTransactions()`.
   - On error: `renderError(txError, err)`, restore button.
8. Implement `editTransaction(id)`:
   - `apiFetch('/api/v1/transactions/' + id)`.
   - Pre-fill form fields from returned data.
   - `currentEditId = id`.
   - Update `#tx-form-toggle` text to "Edit Transaction", `#tx-btn` text to "Update Transaction", show `#tx-cancel`, open `<details>`.
9. Implement `deleteTransaction(id)`:
   - `confirm("Delete this transaction?")`. Return if cancelled.
   - `apiFetch('/api/v1/transactions/' + id, { method: 'DELETE' })`.
   - On success: `loadTransactions()`.
10. Implement `resetForm()`: reset `#tx-form`, set `currentEditId = null`, restore labels "Add Transaction", hide `#tx-cancel`.

**Acceptance**: Transactions table loads paginated. Filters narrow the list and reset pagination. Add a transaction → appears in list. Edit pre-populates form, update persists. Delete with confirm → row removed.

**Depends on**: T004, T005, T006, T015

---

### [X] T018 [P] — Phase 5 gate verification

**File**: Manual test — no file changes.

**Steps**:
1. Navigate to `#transactions` → verify table renders with pagination controls.
2. Add a transaction → verify it appears and form clears.
3. Click Edit on a transaction → verify form pre-populates. Update → verify persists.
4. Delete a transaction with confirmation → verify row removed.
5. Apply type filter → verify list narrows. Apply category filter → verify list narrows. Clear filters → verify all transactions return.
6. With more than 20 transactions: verify Previous is disabled on page 1, Next is disabled on last page.

**Acceptance**: All six checks pass with zero console errors.

**Depends on**: T017

---

## Phase 6 — Budgets View

### [X] T019 — Add budgets view HTML to `index.html` and implement `js/budgets.js`

**File(s)**:
- `finance-tracker/src/main/resources/static/index.html`
- `finance-tracker/src/main/resources/static/js/budgets.js`

**Steps** — HTML (`index.html`):
1. Inside `<section id="view-budgets" class="view">`, add:
   - `<h2>Budgets</h2>`.
   - A `<section>` with `<form id="budget-form">`:
     - `<input type="hidden" id="budget-id">`.
     - Category: `<label for="budget-category">Category</label>` + `<select id="budget-category" required></select>`.
     - Limit: `<label for="budget-limit">Limit Amount</label>` + `<input type="number" id="budget-limit" min="0.01" step="0.01" required>`.
     - Month: `<label for="budget-month">Month</label>` + `<input type="number" id="budget-month" min="1" max="12" required>`.
     - Year: `<label for="budget-year">Year</label>` + `<input type="number" id="budget-year" min="2000" max="2100" required>`.
     - Error: `<div id="budget-error" class="error-msg" hidden></div>`.
     - Buttons: `<button type="submit" id="budget-btn">Add Budget</button>` + `<button type="button" id="budget-cancel" hidden>Cancel</button>`.
   - A `<section>` with `<div id="budget-list"><!-- Populated by budgets.js --></div>`.

**Steps** — JS (`budgets.js`):
1. Import `apiFetch`, `formatMoney`, `renderError`, `setLoading` from `./api.js`.
2. Import `redirectIfUnauth` from `./auth.js`.
3. Import `getCategories` from `./categories.js`.
4. Declare `let currentEditId = null;`.
5. Export `init(container)`:
   - `redirectIfUnauth()` guard.
   - `getCategories()` → populate `#budget-category` select.
   - `loadBudgets()`.
   - Wire `#budget-form` submit → `handleBudgetSubmit()`; `#budget-cancel` → `resetBudgetForm()`.
6. Implement `loadBudgets()`:
   - `apiFetch('/api/v1/budgets')`.
   - Render `#budget-list` as `<table>` with columns: Category, Month/Year, Limit, Spent, Remaining (computed as `limitAmount − spentAmount`), Status (over-limit badge or "OK"), Actions (Edit Limit, Delete).
   - For rows where `budget.overLimit === true`: add class `over-limit` to `<tr>` and display `.over-limit-badge` in Status column.
   - Empty state: "No budgets defined yet."
   - On error: `renderError` in `#budget-list`.
7. Implement `handleBudgetSubmit()`:
   - Validate: category selected, limit > 0, month 1–12, year present.
   - `setLoading(budgetBtn, true, currentEditId ? 'Update Budget' : 'Add Budget')`.
   - If `currentEditId` is null: POST `/api/v1/budgets` with all fields.
   - Else: PUT `/api/v1/budgets/${currentEditId}` with `{ limitAmount }` only.
   - On success: `resetBudgetForm()`, `loadBudgets()`.
   - On error: `renderError(budgetError, err)`, restore button.
8. Implement `editBudget(budget)`:
   - `currentEditId = budget.id`.
   - Pre-fill `#budget-limit` with `budget.limitAmount`.
   - Disable `#budget-category`, `#budget-month`, `#budget-year` (lock; only limit is editable on PUT).
   - Show "Update Budget" on button, show `#budget-cancel`.
9. Implement `deleteBudget(id)`:
   - `confirm("Delete this budget?")`. Return if cancelled.
   - `apiFetch('/api/v1/budgets/' + id, { method: 'DELETE' })`. On success: `loadBudgets()`.
10. Implement `resetBudgetForm()`: reset form, `currentEditId = null`, re-enable locked fields, restore "Add Budget" label, hide cancel.

**Acceptance**: Budgets table renders with spent/remaining computed. Over-limit rows show red styling and badge. Add budget works. Edit changes only limit. Delete with confirm works.

**Depends on**: T004, T005, T006, T015

---

### [X] T020 [P] — Phase 6 gate verification

**File**: Manual test — no file changes.

**Steps**:
1. Navigate to `#budgets` → verify all budgets render with computed Remaining column.
2. Add a budget → verify it appears in the table.
3. Create a transaction that exceeds a budget's limit → navigate to `#budgets` → verify the row is red with over-limit badge.
4. Click Edit Limit → verify only the limit field is editable. Submit → verify limit updated.
5. Delete a budget with confirmation → verify removed.

**Acceptance**: All five checks pass with zero console errors.

**Depends on**: T019

---

## Phase 7 — Goals View

### [X] T021 — Add goals view HTML to `index.html` and implement `js/goals.js`

**File(s)**:
- `finance-tracker/src/main/resources/static/index.html`
- `finance-tracker/src/main/resources/static/js/goals.js`

**Steps** — HTML (`index.html`):
1. Inside `<section id="view-goals" class="view">`, add:
   - `<h2>Goals</h2>`.
   - A `<section>` with `<form id="goal-form">`:
     - `<input type="hidden" id="goal-id">`.
     - Name: `<label for="goal-name">Name</label>` + `<input type="text" id="goal-name" required>`.
     - Target Amount: `<label for="goal-target">Target Amount</label>` + `<input type="number" id="goal-target" min="0.01" step="0.01" required>`.
     - Target Date: `<label for="goal-date">Target Date</label>` + `<input type="date" id="goal-date" required>`.
     - Error: `<div id="goal-error" class="error-msg" hidden></div>`.
     - Buttons: `<button type="submit" id="goal-btn">Add Goal</button>` + `<button type="button" id="goal-cancel" hidden>Cancel</button>`.
   - A `<section>` with `<div id="goal-list"><!-- Populated by goals.js --></div>`.

**Steps** — JS (`goals.js`):
1. Import `apiFetch`, `formatMoney`, `formatDate`, `renderError`, `setLoading` from `./api.js`.
2. Import `redirectIfUnauth` from `./auth.js`.
3. Declare `let currentEditId = null;`.
4. Export `init(container)`:
   - `redirectIfUnauth()` guard.
   - `loadGoals()`.
   - Wire `#goal-form` submit → `handleGoalSubmit()`; `#goal-cancel` → `resetGoalForm()`.
   - Use event delegation on `#goal-list` for Edit clicks, Delete clicks, and contribution form submits (match by `data-goal-id` attribute and CSS class).
5. Implement `loadGoals()`:
   - `apiFetch('/api/v1/goals')`.
   - Render `#goal-list` as cards or table rows. Each row shows: Name, Target (`formatMoney`), Contributed (`formatMoney`), Remaining (`formatMoney(targetAmount - contributedAmount)`), Progress (`goal.progressPercentage + '%'`), Target Date (`formatDate`), Status badge, contribution form, and Edit/Delete buttons.
   - Goals with `status === 'ACHIEVED'`: add class `achieved` to the row/card; render `<span class="achieved-badge">Achieved</span>`.
   - Each goal's inline contribution form:
     ```html
     <form class="contribution-form" data-goal-id="{{goal.id}}">
       <input type="number" class="contrib-amount" min="0.01" step="0.01" placeholder="Amount" required>
       <button type="submit">Add Contribution</button>
       <span class="contrib-error error-msg" hidden></span>
     </form>
     ```
   - Empty state: "No goals defined yet."
   - On error: `renderError` in `#goal-list`.
6. Implement `handleGoalSubmit()`:
   - Validate: name non-empty, target > 0, date present.
   - `setLoading(goalBtn, true, currentEditId ? 'Update Goal' : 'Add Goal')`.
   - POST or PUT depending on `currentEditId`.
   - On success: `resetGoalForm()`, `loadGoals()`.
   - On error: `renderError(goalError, err)`, restore button.
7. Implement `handleContribution(goalId, amount, contribErrorEl)`:
   - `apiFetch('/api/v1/goals/' + goalId + '/contributions', { method: 'POST', body: JSON.stringify({ amount }) })`.
   - On success: `loadGoals()` to refresh progress.
   - On error: set `contribErrorEl.hidden = false; contribErrorEl.textContent = err.message`.
8. Implement `editGoal(goal)`: pre-fill form, set `currentEditId`, update labels, show cancel.
9. Implement `deleteGoal(id)`: `confirm(...)`, DELETE endpoint, reload on success.
10. Implement `resetGoalForm()`: reset form, `currentEditId = null`, restore labels, hide cancel.

**Acceptance**: Goals list renders. Add a goal. Add contributions → progress percentage updates. Goal reaching 100% shows ACHIEVED badge. Edit and delete work.

**Depends on**: T004, T005, T006

---

### [X] T022 [P] — Phase 7 gate verification

**File**: Manual test — no file changes.

**Steps**:
1. Navigate to `#goals` → verify goals list renders (or empty state).
2. Add a goal → verify it appears with 0% progress.
3. Submit a contribution → verify progress percentage updates in the list.
4. Contribute enough to reach the target amount → verify ACHIEVED badge appears.
5. Edit a goal → verify form pre-populates. Update → verify persists.
6. Delete a goal with confirmation → verify removed.

**Acceptance**: All six checks pass with zero console errors.

**Depends on**: T021

---

## Phase 8 — Monthly Trend View

### [X] T023 — Add trend view HTML to `index.html` and implement `js/trend.js`

**File(s)**:
- `finance-tracker/src/main/resources/static/index.html`
- `finance-tracker/src/main/resources/static/js/trend.js`

**Steps** — HTML (`index.html`):
1. Inside `<section id="view-trend" class="view">`, add:
   - `<h2>Monthly Trend</h2>`.
   - A `<section>` containing:
     - `<form id="trend-filter-form">` with `<label for="trend-start">From</label>`, `<input type="date" id="trend-start">`, `<label for="trend-end">To</label>`, `<input type="date" id="trend-end">`, `<button type="submit">Apply</button>`.
     - `<div id="trend-error" class="error-msg" hidden></div>`.
     - `<canvas id="trendChart" height="120"></canvas>`.
     - `<div id="trend-table"><!-- Populated by trend.js --></div>`.

**Steps** — JS (`trend.js`):
1. Import `apiFetch`, `formatMoney`, `renderError` from `./api.js`.
2. Import `redirectIfUnauth` from `./auth.js`.
3. Declare `let trendChartInstance = null;`.
4. Export `init(container)`:
   - `redirectIfUnauth()` guard.
   - `loadTrend()` with no arguments.
   - Wire `#trend-filter-form` submit → read `#trend-start` and `#trend-end`, call `loadTrend(startDate, endDate)`.
5. Implement `loadTrend(startDate, endDate)`:
   - Build `URLSearchParams` from non-empty date params.
   - `apiFetch('/api/v1/reports/monthly-trend?' + params)`.
   - On success:
     - Render `#trend-table` as `<table>` with columns: Month/Year, Income, Expenses, Net Balance. Use `formatMoney()` for amounts.
     - If response array is empty, show "No trend data for this period."
     - Destroy existing chart: `if (trendChartInstance) { trendChartInstance.destroy(); trendChartInstance = null; }`.
     - Create new `Chart` instance on canvas `#trendChart`:
       - Type: `'bar'`.
       - Labels: month-year strings derived from each data point (use `monthYear` field, or construct from `year` and `month` fields depending on API response shape — add a TODO comment to confirm field name on first integration).
       - Datasets: Income (green, `backgroundColor: 'rgba(56,161,105,0.7)'`), Expenses (red, `backgroundColor: 'rgba(229,62,62,0.7)'`), Net Balance (blue, `backgroundColor: 'rgba(66,153,225,0.7)'`).
     - Assign new instance to `trendChartInstance`.
   - On error: `renderError(trendError, err)`.

**Acceptance**: Trend view shows table and chart on load. Applying a date range updates both table and chart. No canvas reuse errors appear in the console. Chart destroys and re-creates cleanly on each filter application.

**Depends on**: T004, T005, T006

---

### [X] T024 [P] — Phase 8 gate verification

**File**: Manual test — no file changes.

**Steps**:
1. Navigate to `#trend` → verify table and bar chart render.
2. Apply a custom date range → verify both table and chart update.
3. Apply the date range a second time → verify no canvas reuse console errors.
4. Verify empty state message when no data exists for the specified range.

**Acceptance**: All four checks pass with zero console errors.

**Depends on**: T023

---

## Phase 9 — Profile View

### [X] T025 — Add profile view HTML to `index.html` and implement `js/profile.js`

**File(s)**:
- `finance-tracker/src/main/resources/static/index.html`
- `finance-tracker/src/main/resources/static/js/profile.js`

**Steps** — HTML (`index.html`):
1. Inside `<section id="view-profile" class="view">`, add:
   - `<h2>My Account</h2>`.
   - A `<section>` with `<div id="profile-info"><!-- Populated by profile.js --></div>`.
   - A `<section>` with `<h3>Update Display Name</h3>` and `<form id="name-form">`:
     - `<label for="display-name">Display Name</label>` + `<input type="text" id="display-name">`.
     - `<div id="name-msg" hidden></div>`.
     - `<button type="submit" id="name-btn">Update</button>`.
   - A `<section>` with `<h3>Change Password</h3>` and `<form id="password-form">`:
     - `<label for="current-password">Current Password</label>` + `<input type="password" id="current-password" required>`.
     - `<label for="new-password">New Password</label>` + `<input type="password" id="new-password" required>`.
     - `<div id="password-msg" hidden></div>`.
     - `<button type="submit" id="password-btn">Change Password</button>`.
   - A `<section>` with `<h3>Delete Account</h3>`, `<p>This action is permanent and cannot be undone.</p>`, `<button id="delete-account-btn" class="secondary">Delete My Account</button>`, `<div id="delete-msg" hidden></div>`.

**Steps** — JS (`profile.js`):
1. Import `apiFetch`, `formatMoney`, `renderError`, `setLoading` from `./api.js`.
2. Import `redirectIfUnauth`, `clearToken` from `./auth.js`.
3. Export `init(container)`:
   - `redirectIfUnauth()` guard.
   - `loadProfile()`.
   - Wire `#name-form` submit → `handleNameUpdate()`.
   - Wire `#password-form` submit → `handlePasswordChange()`.
   - Wire `#delete-account-btn` click → `handleDeleteAccount()`.
4. Implement `loadProfile()`:
   - `apiFetch('/api/v1/users/me')`.
   - Render `#profile-info` with `<p>Email: ${user.email}</p>` and `<p>Display Name: ${user.displayName || '—'}</p>`.
   - Pre-fill `#display-name` with `user.displayName || ''`.
   - On error: `renderError` in `#profile-info`.
5. Implement `handleNameUpdate()`:
   - `event.preventDefault()`. Read `#display-name` value.
   - `setLoading(nameBtn, true, 'Update')`.
   - PUT `/api/v1/users/me` with `{ displayName }`.
   - On success: show `<span class="success-msg">Display name updated.</span>` in `#name-msg`. `setLoading(nameBtn, false, 'Update')`.
   - On error: `renderError(nameMsg, err)`, restore button. Set `#name-msg.hidden = false`.
6. Implement `handlePasswordChange()`:
   - `event.preventDefault()`. Validate both fields non-empty.
   - `setLoading(passwordBtn, true, 'Change Password')`.
   - PUT `/api/v1/users/me/password` with `{ currentPassword, newPassword }`. Response is `204 No Content`.
   - On success: reset `#password-form`, show success message in `#password-msg`. `setLoading(passwordBtn, false, 'Change Password')`.
   - On error: `renderError(passwordMsg, err)`, restore button.
7. Implement `handleDeleteAccount()`:
   - `confirm("Are you sure you want to permanently delete your account? This cannot be undone.")`. Return if cancelled.
   - DELETE `/api/v1/users/me`.
   - On success: `clearToken()`, `location.hash = '#login'`.
   - On error: set `#delete-msg.hidden = false; #delete-msg.textContent = err.message`.

**Acceptance**: Profile shows email and display name. Update display name persists. Change password with wrong current password shows API error. Delete account confirms, clears token, redirects.

**Depends on**: T004, T005, T006

---

### [X] T026 [P] — Phase 9 gate verification

**File**: Manual test — no file changes.

**Steps**:
1. Navigate to `#profile` → verify email and display name are shown.
2. Update display name → verify success message and persisted change on reload.
3. Submit change password with wrong current password → verify API error message appears.
4. Submit change password with correct current password → verify success message, form clears.
5. Click Delete Account, cancel at confirmation → verify nothing happens.
6. Click Delete Account, confirm → verify token is cleared and `#login` view is shown.

**Acceptance**: All six checks pass with zero console errors.

**Depends on**: T025

---

## Phase 10 — Polish, Edge Cases, and Cross-Cutting Concerns

### [X] T027 [P] — Loading state pattern audit across all view modules

**File(s)**:
- `finance-tracker/src/main/resources/static/js/api.js`
- `finance-tracker/src/main/resources/static/js/dashboard.js`
- `finance-tracker/src/main/resources/static/js/transactions.js`
- `finance-tracker/src/main/resources/static/js/budgets.js`
- `finance-tracker/src/main/resources/static/js/goals.js`
- `finance-tracker/src/main/resources/static/js/categories.js`
- `finance-tracker/src/main/resources/static/js/trend.js`
- `finance-tracker/src/main/resources/static/js/profile.js`

**Steps**:
1. Verify `setLoading(btn, loading, defaultLabel)` is exported from `api.js` (added in T004).
2. For every form submit handler in every view module, confirm the pattern:
   - `setLoading(btn, true, label)` is called before `apiFetch`.
   - `setLoading(btn, false, label)` is called in both the success path and the error path (or in a `finally` block).
3. Fix any handler that restores the button in only one branch.
4. Verify no form submission can be triggered twice while a request is in flight (button is disabled).

**Acceptance**: Every form submit handler in every view follows the `setLoading` pattern. Double-submission is impossible while any API call is in flight.

**Depends on**: T013, T015, T017, T019, T021, T023, T025

---

### [X] T028 [P] — Empty state messages audit across all list containers

**File(s)**:
- `finance-tracker/src/main/resources/static/js/dashboard.js`
- `finance-tracker/src/main/resources/static/js/transactions.js`
- `finance-tracker/src/main/resources/static/js/budgets.js`
- `finance-tracker/src/main/resources/static/js/goals.js`
- `finance-tracker/src/main/resources/static/js/categories.js`
- `finance-tracker/src/main/resources/static/js/trend.js`

**Steps**:
1. For each of these containers, verify an empty-state message is rendered when the API returns an empty array or `content: []`:
   - `#tx-list` → "No transactions found."
   - `#budget-list` → "No budgets defined yet."
   - `#goal-list` → "No goals defined yet."
   - `#cat-list` → "No categories found."
   - `#over-limit-list` → "No budgets are currently over their limit."
   - `#trend-table` → "No trend data for this period."
   - `#category-breakdown` → "No category data for this period."
2. Add empty-state rendering to any container that is currently missing it.
3. Verify the empty-state text is never a blank `<div>` or missing entirely.

**Acceptance**: Every list container shows a meaningful message when its data source is empty.

**Depends on**: T013, T015, T017, T019, T021, T023

---

### [X] T029 [P] — Pagination controls disabled-state audit

**File**: `finance-tracker/src/main/resources/static/js/transactions.js`

**Steps**:
1. In `loadTransactions()`, verify:
   - `#tx-prev` button: `disabled` is set to `true` when `data.first === true`, `false` otherwise.
   - `#tx-next` button: `disabled` is set to `true` when `data.last === true`, `false` otherwise.
   - `#tx-page-info` displays `"Page ${data.number + 1} of ${data.totalPages}"` even when `totalPages === 0` (show "Page 0 of 0" or "No results").
2. Fix any missing disable logic.

**Acceptance**: Previous button is always disabled on page 1. Next button is always disabled on the last page. Page info is always visible.

**Depends on**: T017

---

### [X] T030 [P] — `fieldErrors` display in all form error containers

**File(s)**:
- `finance-tracker/src/main/resources/static/js/api.js`

**Steps**:
1. Verify `renderError(errorEl, err)` (exported from `api.js`) handles both cases:
   - When `err.fieldErrors` is a non-empty array: builds a `<ul>` with `<li>field: message</li>` items.
   - When `err.fieldErrors` is absent or empty: sets `errorEl.textContent = err.message || 'An unexpected error occurred.'`.
2. Verify all form error containers in every view pass through `renderError` rather than directly setting `textContent`.
3. Trigger a validation failure from the backend (e.g., submit an empty amount) and confirm the field-level error list renders correctly in the browser.

**Acceptance**: Validation failure responses from the API render individual field errors. Generic errors render the top-level `message`. Raw JSON is never shown to users.

**Depends on**: T004

---

### [X] T031 [P] — 401 global redirect guard verification

**File**: `finance-tracker/src/main/resources/static/js/api.js`

**Steps**:
1. Verify `let redirectingToLogin = false;` is declared at module scope in `api.js`.
2. Verify the 401 handler in `apiFetch` checks and sets this flag before calling `clearToken()` and redirecting.
3. Simulate simultaneous 401 responses (e.g., dashboard loading both summary and budgets simultaneously with an expired token) and confirm only one redirect to `#login` occurs and only one error is thrown.

**Acceptance**: Multiple concurrent 401 responses result in exactly one redirect to `#login` and exactly one token clear. No duplicate navigation events.

**Depends on**: T004

---

### [X] T032 [P] — Category cache invalidation verification

**File**: `finance-tracker/src/main/resources/static/js/categories.js`

**Steps**:
1. Verify that `categoriesCache = null` is set in both the success path of `handleAddCategory()` and the success path of `deleteCategory()`.
2. Verify the code comment explaining cache invalidation is present in `categories.js`.
3. Verify that `getCategories()` fetches fresh data when `categoriesCache` is `null` and returns cached data otherwise.
4. Navigate to `#categories`, add a category, navigate to `#transactions`, open the category dropdown → verify the new category appears (cache was invalidated).

**Acceptance**: Cache is invalidated on every mutation. Fresh data is loaded on the next `getCategories()` call after invalidation.

**Depends on**: T015

---

### [X] T033 [P] — No JWT or password logged to console — security audit

**File(s)**:
- `finance-tracker/src/main/resources/static/js/api.js`
- `finance-tracker/src/main/resources/static/js/auth.js`
- `finance-tracker/src/main/resources/static/js/router.js`

**Steps**:
1. Search all JS files for `console.log`, `console.warn`, `console.error`, `console.debug` calls.
2. For any that exist, verify the logged values do not include the JWT token, any password value, or any raw API response body that could contain credentials.
3. Remove or redact any logging that could expose sensitive values.

**Acceptance**: No JWT value and no password value appears in the browser console under any code path.

**Depends on**: T003, T004, T005

---

### [X] T034 [P] — Responsive nav bar verification

**File**: `finance-tracker/src/main/resources/static/css/styles.css`

**Steps**:
1. Verify `#app-nav ul { flex-wrap: wrap; gap: 0.5rem; }` is present in `styles.css` (added in T006).
2. Open the application in Chrome DevTools at 375px viewport width.
3. Verify nav links wrap to multiple lines without overflowing or becoming unusable.
4. Verify all forms remain usable (inputs and buttons are full-width or appropriately sized) at 375px.
5. If Pico.css handles the responsive behaviour adequately, note that no additional CSS is required. Add minimal CSS only if link overflow is observed.

**Acceptance**: Nav bar is usable at 375px. All forms are usable at 375px. No horizontal overflow scroll is present.

**Depends on**: T006

---

### [X] T035 [P] — `API_BASE` single-location configuration verification

**File**: `finance-tracker/src/main/resources/static/js/api.js`

**Steps**:
1. Verify `export const API_BASE = '';` is the only location where the API base URL is defined across the entire codebase.
2. Verify no view module constructs its own base URL string directly.
3. Verify the comment in `api.js` instructs developers to change `API_BASE` to `http://localhost:8080` only when running from a separate origin.

**Acceptance**: `API_BASE` appears in exactly one file. All `apiFetch` calls derive from it. Switching environments requires editing one value in one file.

**Depends on**: T004

---

### [X] T036 [P] — ES module usage and no-framework compliance audit

**File(s)**: All files in `finance-tracker/src/main/resources/static/js/`

**Steps**:
1. Verify `router.js` is loaded with `<script type="module">` in `index.html`.
2. Verify all inter-module references use ES module `import` / `export` syntax.
3. Verify no file uses `require()`, CommonJS `module.exports`, or any module bundler syntax.
4. Verify no JavaScript framework (React, Vue, Angular, etc.) is loaded or referenced anywhere.
5. Verify no build configuration files (`package.json`, `webpack.config.js`, `vite.config.js`, `.babelrc`, etc.) are present inside `finance-tracker/src/main/resources/static/`.

**Acceptance**: All JS files use ES2020+ module syntax. No framework or build tool artefacts exist.

**Depends on**: T005

---

### [X] T037 [P] — Monetary amount display consistency audit

**File(s)**:
- `finance-tracker/src/main/resources/static/js/api.js`
- `finance-tracker/src/main/resources/static/js/dashboard.js`
- `finance-tracker/src/main/resources/static/js/transactions.js`
- `finance-tracker/src/main/resources/static/js/budgets.js`
- `finance-tracker/src/main/resources/static/js/goals.js`

**Steps**:
1. Verify `formatMoney(value)` is the only function used for displaying monetary amounts across all view modules.
2. Verify no view module calls `parseFloat()` or `toFixed()` directly for display purposes outside of `formatMoney`.
3. Verify all displayed amounts are prefixed with `$` and have exactly two decimal places.
4. Verify `formatDate(isoDate)` is the only function used for displaying dates and that it uses the local-time constructor (`new Date(year, month - 1, day)`) to avoid UTC off-by-one.

**Acceptance**: All monetary amounts display as `$X,XXX.XX`. All dates use the local-time constructor. No raw `parseFloat` or `toFixed` calls appear outside of `api.js`.

**Depends on**: T004, T013, T017, T019, T021

---

### [X] T038 [P] — Client-side validation audit across all forms

**File(s)**:
- `finance-tracker/src/main/resources/static/js/router.js`
- `finance-tracker/src/main/resources/static/js/transactions.js`
- `finance-tracker/src/main/resources/static/js/budgets.js`
- `finance-tracker/src/main/resources/static/js/goals.js`
- `finance-tracker/src/main/resources/static/js/categories.js`
- `finance-tracker/src/main/resources/static/js/profile.js`

**Steps**:
1. For the login form: verify email non-empty and contains `@`; password non-empty — before `apiFetch`.
2. For the register form: same email and password rules.
3. For the transaction form: amount > 0, type selected, category selected, date non-empty.
4. For the budget form: category selected, limit > 0, month 1–12, year present.
5. For the goal form: name non-empty, target > 0, date present.
6. For the category form: name non-empty.
7. For the profile password form: both current password and new password non-empty.
8. Verify each validation failure shows an inline error message without making an API call.

**Acceptance**: All required-field and positive-number validations fire client-side before any API request. Failing validation shows an inline message and does not submit.

**Depends on**: T009, T010, T015, T017, T019, T021, T025

---

### [X] T039 [P] — Delete confirmation dialogs audit

**File(s)**:
- `finance-tracker/src/main/resources/static/js/transactions.js`
- `finance-tracker/src/main/resources/static/js/budgets.js`
- `finance-tracker/src/main/resources/static/js/goals.js`
- `finance-tracker/src/main/resources/static/js/categories.js`
- `finance-tracker/src/main/resources/static/js/profile.js`

**Steps**:
1. Verify every delete operation calls `confirm()` before making any API call.
2. Verify that cancelling the confirmation dialog causes no API call and no UI change.
3. Verify the account deletion confirmation uses the specific message: "Are you sure you want to permanently delete your account? This cannot be undone."

**Acceptance**: All destructive actions require a `confirm()` prompt. Cancelling the prompt leaves the UI unchanged.

**Depends on**: T015, T017, T019, T021, T025

---

### [X] T040 [P] — Static asset HTTP request count verification

**File**: `finance-tracker/src/main/resources/static/index.html`

**Steps**:
1. Open the application in Chrome DevTools Network tab, filter by "Doc", "JS", and "CSS" (excluding CDN resources).
2. Count the number of static asset requests for the local files: `index.html`, `css/styles.css`, `js/router.js`, plus the individual view JS modules loaded as ES module imports.
3. Verify the local static asset request count does not violate NFR-040 (no more than 3 HTTP requests for local static assets before API calls — `index.html`, `css/styles.css`, and `js/router.js` as the entry point; additional ES module imports are loaded lazily by the browser).
4. Note: CDN resources (Pico.css, Chart.js) are excluded from this count per the specification.

**Acceptance**: The application loads with the expected minimal set of static asset requests. CDN resources load without errors.

**Depends on**: T002, T006, T005

---

### [X] T041 [P] — Cross-browser smoke test

**File**: Manual test — no file changes.

**Steps**:
1. Open the application in Chrome (current stable) → complete a full workflow: register, dashboard, add transaction, add budget, add goal, add contribution, view trend, update profile, log out.
2. Repeat the same workflow in Firefox (current stable).
3. Repeat the same workflow in Edge (current stable).
4. In each browser, open DevTools console and verify zero unhandled errors or promise rejections.
5. In each browser, verify `<input type="date">` produces values in `YYYY-MM-DD` format.

**Acceptance**: Full workflow completes without errors in Chrome, Firefox, and Edge. No console errors in any browser.

**Depends on**: T011, T014, T016, T018, T020, T022, T024, T026

---

### [X] T042 — Final spec compliance walkthrough

**File**: Manual test against `spec.md` acceptance scenarios.

**Steps**:
1. Work through every acceptance scenario from `spec.md` in order (Scenarios 1–26):
   - Scenarios 1–5: Authentication flows (register, login, failed login, 401 expiry, logout).
   - Scenarios 6–7: Dashboard summary and date range.
   - Scenarios 8–12: Transactions CRUD, pagination, filters.
   - Scenarios 13–16: Budgets CRUD, over-limit indicator.
   - Scenarios 17–20: Goals CRUD, contributions, ACHIEVED badge.
   - Scenarios 21–24: Categories CRUD, system-default guard, 409 handling.
   - Scenarios 25–26: Monthly trend table and chart with date range.
2. Verify all edge cases from `spec.md`:
   - API errors (4xx, 5xx) always show a user-readable message; no raw JSON in the page.
   - No JWT in `localStorage` on any protected view → immediate redirect to `#login`.
   - Form submissions disabled during in-flight requests; no duplicate submissions.
   - Pagination Previous/Next disabled on first/last page.
   - Empty states show meaningful messages.
3. Open DevTools console during the full walkthrough. Zero unhandled errors or promise rejections are required.
4. Mark implementation complete only when all 26 scenarios and all edge cases pass.

**Acceptance**: All 26 acceptance scenarios pass. All edge cases pass. Zero console errors throughout the walkthrough.

**Depends on**: T027–T041

---

## Dependencies Graph

```
T001 (directory structure)
  ├── T002 (index.html skeleton)
  │     ├── T007 (login HTML)
  │     │     └── T009 (login handler)
  │     ├── T008 (register HTML)
  │     │     └── T010 (register handler)
  │     ├── T012 (dashboard HTML)
  │     ├── T015 (categories HTML + JS)  ←── also needs T004, T005, T006
  │     ├── T017 (transactions HTML + JS) ←── also needs T004, T005, T006, T015
  │     ├── T019 (budgets HTML + JS)     ←── also needs T004, T005, T006, T015
  │     ├── T021 (goals HTML + JS)       ←── also needs T004, T005, T006
  │     ├── T023 (trend HTML + JS)       ←── also needs T004, T005, T006
  │     └── T025 (profile HTML + JS)     ←── also needs T004, T005, T006
  ├── T003 (auth.js)
  │     └── T004 (api.js)
  │           └── T005 (router.js)
  └── T006 (styles.css)

T009, T010 → T011 (Phase 2 gate)
T013 → T014 (Phase 3 gate)   [T013 depends on T004, T005, T012]
T015 → T016 (Phase 4 gate)
T017 → T018 (Phase 5 gate)
T019 → T020 (Phase 6 gate)
T021 → T022 (Phase 7 gate)
T023 → T024 (Phase 8 gate)
T025 → T026 (Phase 9 gate)

T013, T015, T017, T019, T021, T023, T025 → T027–T039 (polish tasks, all [P])
T027–T041 → T042 (final compliance walkthrough)
```

---

## Parallel Execution Groups

Tasks marked `[P]` have no shared file dependencies with other `[P]` tasks in the same group and can be executed simultaneously by different developers.

### Group A — Phase 3–9 View Implementation (after T011 is complete)

These tasks touch different JS modules and different sections of `index.html`. They can proceed in parallel with the exception that T017 and T019 must wait for T015 (categories) to be complete first.

```
T013 [P]  dashboard.js
T015      categories HTML + JS    ← must complete before T017 and T019
T021 [P]  goals HTML + JS
T023 [P]  trend HTML + JS
T025 [P]  profile HTML + JS
```

Then, after T015:
```
T017 [P]  transactions HTML + JS
T019 [P]  budgets HTML + JS
```

### Group B — Phase 10 Polish (after all view tasks are complete)

All polish tasks are independent of each other:
```
T027 [P]  Loading state audit
T028 [P]  Empty state audit
T029 [P]  Pagination disabled-state audit
T030 [P]  fieldErrors display audit
T031 [P]  401 guard verification
T032 [P]  Category cache audit
T033 [P]  Security/console audit
T034 [P]  Responsive nav audit
T035 [P]  API_BASE single-location audit
T036 [P]  ES module compliance audit
T037 [P]  Monetary display audit
T038 [P]  Client-side validation audit
T039 [P]  Delete confirmation audit
T040 [P]  Static asset request count
T041 [P]  Cross-browser smoke test
```

---

## Validation Checklist

### Spec Coverage

- [x] FR-001 (no build tools) — covered by T036
- [x] FR-002 (JWT in `localStorage` as `ft_token`) — covered by T003
- [x] FR-003 (401 global redirect) — covered by T004, T031
- [x] FR-004 (Bearer header on all requests) — covered by T004
- [x] FR-005 (single `API_BASE` constant) — covered by T004, T035
- [x] FR-006 (persistent nav on authenticated views) — covered by T005
- [x] FR-007 (hash-based routing) — covered by T005
- [x] FR-010–FR-015 (authentication views) — covered by T007–T011
- [x] FR-020–FR-023 (dashboard) — covered by T012–T014
- [x] FR-030–FR-037 (transactions) — covered by T017–T018
- [x] FR-040–FR-045 (budgets) — covered by T019–T020
- [x] FR-050–FR-054 (goals) — covered by T021–T022
- [x] FR-060–FR-063 (categories) — covered by T015–T016
- [x] FR-070–FR-073 (monthly trend) — covered by T023–T024
- [x] FR-080–FR-083 (profile) — covered by T025–T026
- [x] NFR-001–NFR-003 (technology constraints) — covered by T036
- [x] NFR-010–NFR-013 (security) — covered by T003, T004, T033
- [x] NFR-020–NFR-023 (usability) — covered by T027, T028, T038
- [x] NFR-030–NFR-032 (file organization) — covered by T001, T036
- [x] NFR-040–NFR-041 (performance) — covered by T015 (cache), T040

### Task Completeness

- [x] All 9 views have a dedicated HTML task and a JS implementation task
- [x] All 9 views have a phase gate verification task
- [x] All shared utilities (`apiFetch`, `formatMoney`, `formatDate`, `setLoading`, `renderError`) are in a single shared file
- [x] `getCategories()` export and cache mechanism specified
- [x] Chart.js destroy-and-recreate pattern specified for trend view
- [x] `currentEditId` dual-mode form pattern specified for transactions, budgets, goals
- [x] All delete operations require `confirm()` prompt
- [x] All form submissions disable button during API call
- [x] Empty states specified for every list container
- [x] Pagination disabled-state logic specified for transactions
- [x] All file paths use `finance-tracker/src/main/resources/static/` prefix

### No Clarification Markers

All requirements from the plan and spec are fully addressed. The monthly-trend API response field name for the month-year label (`monthYear` vs. `year`/`month` fields) is annotated with a TODO comment in T023 as the plan itself identifies this as a medium-risk item to be confirmed on first integration with the backend. This does not block implementation.
