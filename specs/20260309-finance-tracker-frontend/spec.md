# Finance Tracker Frontend — Feature Specification

**Feature Branch**: `feature/finance-tracker-frontend`
**Created**: 2026-03-09
**Status**: Ready for Review
**Input**: Plain HTML/CSS/JavaScript frontend for the Finance Tracker Spring Boot REST API
**Foundation Version**: 1.0.0
**Backend Spec Reference**: `specs/20260309-finance-tracker-api/spec.md`

---

## Overview

This specification describes a plain HTML + CSS + vanilla JavaScript frontend client for the
Finance Tracker REST API. The frontend is a single HTML file (`index.html`) with JavaScript-driven
hash-based view switching (`#login`, `#dashboard`, `#transactions`, `#budgets`, `#goals`,
`#categories`, `#trend`, `#profile`) that allows authenticated users to manage their personal
finances through a browser without requiring any build tools, bundlers, or JavaScript frameworks.

The implementation must cover: user authentication (register/login), a dashboard showing a
financial summary, and full CRUD interfaces for transactions, budgets, goals, and categories.
A monthly trend view consumes the reporting endpoint to display income and expense data
over time with a Chart.js bar or line chart.

The primary goals are correctness and usability, not visual polish. The UI uses Pico.css
(loaded via CDN) for a clean, classless baseline that requires no hand-written layout rules
and introduces no build step.

The frontend files are placed inside
`finance-tracker/src/main/resources/static/` so that Spring Boot serves them directly,
eliminating CORS configuration entirely when accessing the app on the same origin as the API.

---

## User Scenarios & Testing

### Primary User Story

As a registered Finance Tracker user, I want to log in through a browser, view my financial
summary, and manage my transactions, budgets, goals, and categories so that I can track and
control my personal finances without installing any software.

### Acceptance Scenarios

#### Authentication

1. **Given** a new visitor, **When** they complete the registration form with a valid email,
   password, and optional display name, **Then** the browser POSTs to `/api/v1/auth/register`,
   and on success immediately POSTs to `/api/v1/auth/login` with the same credentials, stores
   the returned JWT, and redirects the user to the dashboard.
2. **Given** a registered user, **When** they submit the login form with valid credentials,
   **Then** the browser POSTs to `/api/v1/auth/login`, the returned JWT is stored in
   `localStorage`, and the user is redirected to the dashboard.
3. **Given** a failed login (wrong credentials), **When** the API returns a 4xx response,
   **Then** a clear error message is displayed inline without reloading the page.
4. **Given** an authenticated user whose token has expired (2 hours), **When** they make any
   API request, **Then** the frontend detects the 401 response, clears the stored JWT, and
   redirects to the login view with an explanatory message.
5. **Given** an authenticated user, **When** they click "Log Out", **Then** the JWT is removed
   from `localStorage` and the user is returned to the login view.

#### Dashboard

6. **Given** an authenticated user on the dashboard, **When** the view loads, **Then** the
   frontend calls `GET /api/v1/reports/summary` (defaulting to the current month) and displays
   total income, total expenses, net balance, and a per-category breakdown.
7. **Given** an authenticated user on the dashboard, **When** they change the date-range
   inputs and click "Apply", **Then** the summary is re-fetched and the displayed figures
   update without a full page reload.

#### Transactions

8. **Given** an authenticated user on the transactions list, **When** the view loads, **Then**
   the frontend calls `GET /api/v1/transactions` and renders a paginated table of transactions
   sorted by date descending.
9. **Given** an authenticated user, **When** they submit the "Add Transaction" form with valid
   inputs, **Then** `POST /api/v1/transactions` is called, the new transaction appears in the
   list, and the form is cleared.
10. **Given** an authenticated user, **When** they click "Edit" on a transaction, **Then** the
    form pre-populates with the existing values, and on submission `PUT /api/v1/transactions/{id}`
    is called and the list is refreshed.
11. **Given** an authenticated user, **When** they click "Delete" on a transaction and confirm,
    **Then** `DELETE /api/v1/transactions/{id}` is called and the row is removed from the list.
12. **Given** the transaction list, **When** filter controls (type, category, start date, end date)
    are applied, **Then** the frontend re-fetches the filtered list from the API.

#### Budgets

13. **Given** an authenticated user on the budgets list, **When** the view loads, **Then**
    all budgets are fetched from `GET /api/v1/budgets` and displayed, each showing category,
    limit, spent amount, remaining amount, and an over-limit indicator.
14. **Given** an authenticated user, **When** they create a budget via the form, **Then**
    `POST /api/v1/budgets` is called and the new budget appears in the list.
15. **Given** a budget that is over its limit, **When** it is displayed in the list, **Then**
    it is visually distinguished (e.g., a highlighted row or warning label).
16. **Given** an authenticated user, **When** they edit or delete a budget, **Then** the
    appropriate PUT or DELETE call is made and the list refreshes.

#### Goals

17. **Given** an authenticated user on the goals list, **When** the view loads, **Then**
    all goals are fetched from `GET /api/v1/goals` and displayed with target amount,
    contributed amount, progress percentage, and status.
18. **Given** an authenticated user, **When** they create a goal, **Then** `POST /api/v1/goals`
    is called and the goal appears in the list.
19. **Given** an authenticated user, **When** they submit the "Add Contribution" form for a
    goal, **Then** `POST /api/v1/goals/{id}/contributions` is called and the goal's progress
    updates in the list.
20. **Given** a goal with status `ACHIEVED`, **When** it is displayed, **Then** it is visually
    marked as complete (e.g., a distinct badge or label).

#### Categories

21. **Given** an authenticated user on the categories list, **When** the view loads, **Then**
    all categories (system defaults + custom) are fetched from `GET /api/v1/categories` and
    displayed.
22. **Given** an authenticated user, **When** they create a custom category, **Then**
    `POST /api/v1/categories` is called and the new category appears in the list.
23. **Given** a system-default category, **When** it is displayed in the list, **Then** the
    delete control is absent or disabled for that row.
24. **Given** an authenticated user, **When** they delete a custom category that is in use,
    **Then** the API's `409 Conflict` response is surfaced as a clear error message in the UI.

#### Monthly Trend View

25. **Given** an authenticated user on the monthly trend view, **When** the view loads,
    **Then** `GET /api/v1/reports/monthly-trend` is called and the month-by-month income,
    expense, and net balance figures are rendered in a table and as a Chart.js bar or line chart.
26. **Given** an authenticated user, **When** they apply a custom date range on the trend view,
    **Then** the data is re-fetched with the supplied `startDate` and `endDate` parameters and
    the chart updates.

### Edge Cases

- API errors (4xx, 5xx) MUST always surface a user-readable error message; raw JSON error
  bodies MUST NOT be rendered directly into the page without parsing.
- If `localStorage` does not contain a JWT on any protected view load, the user MUST be
  immediately redirected to the login view.
- Form submissions MUST be disabled (or show a loading indicator) while an API request is
  in flight to prevent duplicate submissions.
- Pagination controls MUST be disabled when on the first page (previous) or last page (next).
- Empty states (no transactions, no budgets, etc.) MUST display a meaningful message rather
  than a blank section.

---

## Requirements

### Functional Requirements

#### General / Application Shell

- **FR-001**: The application MUST function without any build tool, bundler, or transpiler.
  All JavaScript MUST be valid ES2020+ that runs directly in a modern browser (Chrome, Firefox,
  Edge — current stable versions).
- **FR-002**: The application MUST store the JWT access token exclusively in `localStorage`
  under the key `ft_token`.
- **FR-003**: The application MUST intercept all API responses with status `401` and redirect
  the user to the login view, clearing the stored JWT.
- **FR-004**: All authenticated API requests MUST include the JWT in the `Authorization`
  header as `Bearer <token>`.
- **FR-005**: The API base URL MUST be defined in a single, easily configurable location
  (e.g., a `const API_BASE` constant at the top of a shared JavaScript file) so that
  switching between development and production environments requires editing only one value.
  Because the frontend is served from the same origin as the Spring Boot API (inside
  `src/main/resources/static/`), `API_BASE` defaults to an empty string and all paths are
  relative (e.g., `/api/v1/...`).
- **FR-006**: Navigation between views MUST be consistent and accessible from every
  authenticated view. A navigation bar MUST be present on all post-login views and is
  implemented as a persistent section in the single `index.html`.
- **FR-007**: The application MUST be structured as a single `index.html` file with
  JavaScript-driven hash-based view switching. Each view corresponds to a named hash
  fragment (`#login`, `#register`, `#dashboard`, `#transactions`, `#budgets`, `#goals`,
  `#categories`, `#trend`, `#profile`). The router listens to `hashchange` events and shows
  or hides the appropriate `<section>` element.

#### Authentication Pages

- **FR-010**: The login view MUST contain an email field, a password field, and a submit
  button. It MUST also provide a link to the registration view (`#register`).
- **FR-011**: The registration view MUST contain an email field, a password field, an
  optional display name field, and a submit button.
- **FR-012**: Client-side validation MUST check that the email field is non-empty and contains
  an `@` character, and that the password field is non-empty, before submitting to the API.
  More robust validation is deferred to the API.
- **FR-013**: On successful login, the JWT MUST be written to `localStorage` under the key
  `ft_token` and the user MUST be redirected to the dashboard view (`#dashboard`).
- **FR-014**: On successful registration, the frontend MUST immediately POST to
  `/api/v1/auth/login` with the same credentials (auto-login). On success, the JWT is
  stored and the user is redirected to the dashboard view (`#dashboard`). The registration
  endpoint returns `201` but does not return a JWT, so this follow-up call is required.
- **FR-015**: If the user is already authenticated (valid JWT in `localStorage`) and navigates
  to the login or registration view, they MUST be redirected to the dashboard view.

#### Dashboard

- **FR-020**: The dashboard MUST call `GET /api/v1/reports/summary` on view load with no
  date parameters (defaulting to the current calendar month on the server side).
- **FR-021**: The dashboard MUST display: total income, total expenses, net balance, and a
  per-category breakdown table of income and expense totals.
- **FR-022**: The dashboard MUST provide date-range inputs (start date, end date) and an
  "Apply" button to re-fetch the summary for a custom range.
- **FR-023**: The dashboard MUST display a count and list of budgets that are currently over
  their limit (fetched from `GET /api/v1/budgets`, filtered client-side on the `overLimit`
  flag), linking to the budgets view. This is a firm requirement; the second API call on
  dashboard load is accepted.

#### Transactions Page

- **FR-030**: The transactions view MUST display a table of the user's transactions with
  columns: date, type (INCOME / EXPENSE), category, amount, description (truncated if long),
  and action buttons (Edit, Delete).
- **FR-031**: The view MUST support pagination. Controls MUST include "Previous" and "Next"
  buttons and display the current page number. The page size sent to the API MUST NOT exceed
  25 (the backend maximum).
- **FR-032**: Filter controls MUST be provided for: transaction type (INCOME / EXPENSE / All),
  category (dropdown populated from `GET /api/v1/categories`), start date, and end date.
  Changing any filter MUST reset pagination to page 0 and re-fetch.
- **FR-033**: An "Add Transaction" form MUST be present on the view (inline or toggled visible)
  with fields: amount, type (radio or select), category (select), date, and description
  (optional textarea).
- **FR-034**: The category dropdown in the transaction form MUST be populated by calling
  `GET /api/v1/categories` and listing all available categories.
- **FR-035**: Clicking "Edit" on a transaction row MUST populate the form with the existing
  transaction values for in-place editing.
- **FR-036**: Clicking "Delete" on a transaction row MUST show a confirmation prompt
  (browser `confirm()` dialog is acceptable) before calling the delete endpoint.
- **FR-037**: After any create, update, or delete operation succeeds, the transaction list
  MUST be refreshed by re-fetching the current page from the API.

#### Budgets Page

- **FR-040**: The budgets view MUST display all budgets in a table or card layout with:
  category name, month/year, limit amount, spent amount, remaining amount (limit minus spent),
  and an over-limit indicator.
- **FR-041**: Budgets that are over their limit MUST be visually distinguished from budgets
  within their limit (e.g., red text, badge, or highlighted row background).
- **FR-042**: An "Add Budget" form MUST be provided with fields: category (select), limit
  amount, month (1–12), and year.
- **FR-043**: An "Edit" action MUST allow the user to update the limit amount of an existing
  budget.
- **FR-044**: A "Delete" action MUST prompt for confirmation before calling the delete endpoint.
- **FR-045**: The category select in the budget form MUST be populated from
  `GET /api/v1/categories`.

#### Goals Page

- **FR-050**: The goals view MUST display all goals in a table or card layout with: name,
  target amount, contributed amount, remaining amount, progress percentage, target date,
  and status (IN_PROGRESS / ACHIEVED).
- **FR-051**: Goals with status `ACHIEVED` MUST be visually distinguished (e.g., a green
  badge or strikethrough styling).
- **FR-052**: An "Add Goal" form MUST be provided with fields: name, target amount, and
  target date.
- **FR-053**: An "Add Contribution" control MUST be present for each goal (e.g., an inline
  form or a small toggled section) with a single amount field. On submission, it MUST call
  `POST /api/v1/goals/{id}/contributions`.
- **FR-054**: Edit and Delete actions MUST be provided per goal row, with a confirmation
  prompt before deletion.

#### Categories Page

- **FR-060**: The categories view MUST display all categories (system defaults and custom)
  in a table or list with: name and a system/custom indicator.
- **FR-061**: An "Add Category" form MUST be provided with a single name field.
- **FR-062**: System-default categories MUST NOT have a delete button (or the button must be
  absent/disabled). The `isSystemDefault` flag from the API response MUST be used to determine
  this. Rename/edit functionality is omitted because the backend does not expose a
  `PUT /api/v1/categories/{id}` endpoint.
- **FR-063**: Deleting a category that returns `409 Conflict` MUST display a user-readable
  error message (e.g., "This category is in use by existing transactions or budgets and cannot
  be deleted.").

#### Monthly Trend View

- **FR-070**: The monthly trend view MUST call `GET /api/v1/reports/monthly-trend` on load
  (with no date filters, showing whatever default range the server returns).
- **FR-071**: The trend data MUST be rendered in a table showing each month-year row with
  columns: month/year, total income, total expenses, net balance.
- **FR-072**: Date-range inputs (start date, end date) MUST be provided so the user can
  narrow or extend the trend period.
- **FR-073**: The trend data MUST also be rendered as a bar or line chart using Chart.js
  (loaded via CDN). Chart.js is a charting library, not a JavaScript framework, and its
  CDN delivery introduces no build step, satisfying the "no frameworks, no build tools"
  constraint. The chart MUST update when the date range is changed.

#### User Profile / Account Page

- **FR-080**: An account/profile view MUST be accessible from the navigation. It MUST display
  the user's email and display name (fetched from `GET /api/v1/users/me`).
- **FR-081**: The profile view MUST provide a form to update the display name
  (`PUT /api/v1/users/me`).
- **FR-082**: The profile view MUST provide a change-password form with fields: current
  password and new password, submitted to `PUT /api/v1/users/me/password`.
- **FR-083**: The profile view MUST provide a "Delete Account" button with a confirmation
  prompt. On confirmation, `DELETE /api/v1/users/me` is called, the JWT is cleared, and the
  user is redirected to the login view.

### Non-Functional Requirements

#### Technology Constraints

- **NFR-001**: The frontend MUST use only plain HTML5, CSS3, and vanilla JavaScript (ES2020+),
  plus Pico.css via CDN for baseline styling and Chart.js via CDN for the trend chart. No
  JavaScript frameworks (React, Vue, Angular, Svelte, etc.) and no build tools (Webpack,
  Vite, Rollup, Babel, etc.) are permitted.
- **NFR-002**: All JavaScript MUST execute directly in modern browsers without transpilation.
  Browser targets are the current stable versions of Chrome, Firefox, and Edge.
- **NFR-003**: No server-side rendering or server-side logic is introduced by this frontend.
  The frontend is entirely static — HTML, CSS, and JS files served as-is by Spring Boot from
  `src/main/resources/static/`.

#### Security

- **NFR-010**: The JWT MUST be stored in `localStorage` under the key `ft_token`. This choice
  provides persistence across browser tabs and sessions until explicitly cleared, which is the
  desired behavior for this application.
- **NFR-011**: The `Authorization: Bearer <token>` header MUST be attached to every API
  request to a protected endpoint. A centralized `apiFetch()` wrapper function MUST be used
  rather than adding the header ad hoc in each call site.
- **NFR-012**: The frontend MUST NOT log the JWT or any password value to the browser console
  in production-intended code.
- **NFR-013**: The application MUST handle `401 Unauthorized` responses from the API globally
  by clearing the stored token and redirecting to the login view, to avoid states where the
  user appears logged in but all API calls fail.

#### Usability

- **NFR-020**: Loading states MUST be communicated to the user. At minimum, submit buttons
  MUST be disabled and show a "Loading..." label while an API request is in flight.
- **NFR-021**: Success and error messages MUST be displayed inline near the relevant form or
  list, not only in `alert()` dialogs. Browser `alert()` may be used for destructive
  confirmation prompts (delete actions) as a baseline; non-blocking alternatives are preferred.
- **NFR-022**: All forms MUST perform basic client-side validation (empty field checks for
  required fields, positive number checks for amount fields) before submitting to the API,
  to reduce unnecessary round-trips.
- **NFR-023**: The application MUST provide a responsive layout. Pico.css provides responsive
  grid and typography behaviour by default, satisfying this requirement with minimal additional
  CSS. The layout MUST be usable on both desktop (1024px+) and mobile viewports.

#### File Organization

- **NFR-030**: JavaScript MUST be organized into modules or clearly separated files by
  concern (e.g., `api.js` for all fetch logic, `auth.js` for authentication helpers, and
  view-specific scripts). Inline `<script>` blocks that contain more than trivial
  initialization logic are discouraged.
- **NFR-031**: CSS MUST be organized in one or more external stylesheet files. Inline styles
  MUST be avoided except for JavaScript-driven dynamic state (e.g., toggling `display: none`).
  Pico.css is loaded from CDN; a supplemental `css/styles.css` file handles any project-specific
  overrides.
- **NFR-032**: All frontend files MUST be placed inside
  `finance-tracker/src/main/resources/static/`. Spring Boot automatically serves everything
  in this directory as static content on the same origin as the API, so no CORS configuration
  is needed and `API_BASE` can be left as an empty string.

#### Performance

- **NFR-040**: The page load for any view MUST require no more than 3 HTTP requests for
  static assets (HTML, CSS, JS files — excluding CDN resources) before making API calls.
  This encourages minimal file fragmentation without a bundler.
- **NFR-041**: API calls MUST be made only when necessary (e.g., not re-fetching the category
  list on every keystroke). Category data MAY be cached in a JavaScript variable for the
  lifetime of the page session.

---

## Page / View Inventory

| Page / View | Hash | Primary API Calls |
|---|---|---|
| Login | `#login` | POST `/api/v1/auth/login` |
| Register | `#register` | POST `/api/v1/auth/register`, POST `/api/v1/auth/login` (auto-login) |
| Dashboard | `#dashboard` | GET `/api/v1/reports/summary`, GET `/api/v1/budgets` |
| Transactions | `#transactions` | GET/POST/PUT/DELETE `/api/v1/transactions`, GET `/api/v1/categories` |
| Budgets | `#budgets` | GET/POST/PUT/DELETE `/api/v1/budgets`, GET `/api/v1/categories` |
| Goals | `#goals` | GET/POST/PUT/DELETE `/api/v1/goals`, POST `/api/v1/goals/{id}/contributions` |
| Categories | `#categories` | GET/POST/DELETE `/api/v1/categories` |
| Monthly Trend | `#trend` | GET `/api/v1/reports/monthly-trend` |
| Profile / Account | `#profile` | GET/PUT `/api/v1/users/me`, PUT `/api/v1/users/me/password`, DELETE `/api/v1/users/me` |

---

## Proposed File Structure

```
finance-tracker/
└── src/
    └── main/
        └── resources/
            └── static/
                ├── index.html              Single-page entry point; all views are sections
                ├── css/
                │   └── styles.css          Project-specific overrides on top of Pico.css
                └── js/
                    ├── api.js              apiFetch() wrapper, API_BASE constant, 401 handling
                    ├── auth.js             getToken(), setToken(), clearToken(), redirectIfUnauth()
                    ├── router.js           Hash-based view switcher, hashchange listener
                    ├── dashboard.js
                    ├── transactions.js
                    ├── budgets.js
                    ├── goals.js
                    ├── categories.js
                    ├── trend.js            Includes Chart.js integration
                    └── profile.js
```

The CDN resources referenced in `index.html` are:

```html
<!-- Pico.css — classless baseline styles -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css">

<!-- Chart.js — trend chart -->
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
```

---

## API Integration Details

### Base URL Configuration

```js
// js/api.js
// Empty string — frontend is served from the same origin as the Spring Boot API.
// Change to 'http://localhost:8080' only if running the frontend from a separate origin.
const API_BASE = '';
```

### Centralized Fetch Wrapper

All API calls MUST go through an `apiFetch()` function that:
1. Prepends `API_BASE` to the path.
2. Attaches `Authorization: Bearer <token>` from `localStorage` (if a token exists).
3. Sets `Content-Type: application/json` on requests with a body.
4. Awaits the response and, if the status is `401`, calls `auth.clearToken()` and redirects
   to the login view (`#login`).
5. For non-2xx responses, rejects with the parsed error body (or a generic message if
   parsing fails), so callers can display meaningful error text.

### JWT Lifecycle

| Event | Action |
|---|---|
| Successful login | Store token: `localStorage.setItem('ft_token', token)` |
| Any 401 response | Clear token: `localStorage.removeItem('ft_token')`, redirect to `#login` |
| Logout click | Clear token: `localStorage.removeItem('ft_token')`, redirect to `#login` |
| View load (protected) | Check token presence; redirect to `#login` if absent |

Note: The frontend has no way to proactively detect token expiry without a server round-trip.
Expiry is handled reactively: the first 401 response triggers logout and redirect.

### Pagination Handling

- All list endpoints support `page` (0-indexed) and `size` parameters.
- The frontend MUST use `size=20` (the server default, within the 25-record maximum).
- The API returns a Spring `Page` object containing `content`, `totalPages`, `totalElements`,
  `number` (current page), `first`, and `last` boolean fields. These MUST be used to drive
  the pagination controls.

### Error Display

When an API call fails, the frontend MUST:
1. Parse the response body as JSON.
2. Extract the `message` field from the error object (schema: `{ timestamp, status, error, message, path }`).
3. Display that message in a visible error element near the form or list that triggered the call.
4. For `fieldErrors` arrays (validation failures), display each field's message.

---

## Key Implementation Notes

### Category Caching

The category list is needed on multiple views (transactions form, budgets form). Each view
fetches it independently on load. Because the application is a single-page app with hash
routing, a module-level variable in `categories.js` (or `api.js`) MAY cache the list for
the lifetime of the browser session to avoid redundant round-trips.

### Form Mode (Add vs. Edit)

Transaction, budget, goal, and category forms serve dual purpose: add mode and edit mode.
A simple pattern is to track a `currentEditId` variable per view module. When `currentEditId`
is `null`, the form submit calls POST; when it is set, the form submit calls PUT with that ID.
The form heading and submit button label SHOULD change to reflect the current mode.

### Monetary Display

All amounts returned by the API are `BigDecimal` values serialized by Spring Boot as JSON
numbers (e.g., `1234.5600`). The frontend MUST parse them with `parseFloat()` and format
for display with `toFixed(2)`. All monetary amounts MUST be prefixed with a hardcoded `$`
symbol (e.g., `$1,234.56`). The currency symbol is not configurable in the initial release.

Example formatting helper:

```js
function formatMoney(value) {
  return '$' + parseFloat(value).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}
```

### Date Formatting

Dates from the API are ISO 8601 strings (`YYYY-MM-DD` for `LocalDate`). The frontend MUST
display dates in a human-readable format. The locale-formatted date (using
`new Date(dateStr).toLocaleDateString()`) is acceptable. Date inputs in forms MUST use
`<input type="date">` which natively produces and accepts `YYYY-MM-DD` format, matching the
API contract.

### Chart.js Integration (Monthly Trend)

Chart.js is loaded from CDN in `index.html`. The `trend.js` module MUST:
1. Render a `<canvas id="trendChart">` element inside the trend view section.
2. Create a `Chart` instance (bar or line type) using the monthly-trend API response data.
3. Destroy and re-create (or `update()`) the chart instance when the date range changes, to
   avoid canvas reuse errors.

### Confirmation Dialogs

Browser-native `confirm()` dialogs are acceptable for delete confirmations in the initial
release. They are synchronous, universally supported, and require no additional HTML or JS.

---

## Assumptions Made

1. The Spring Boot backend is already running and accessible. Because the frontend is served
   from `src/main/resources/static/`, it shares the same origin as the API
   (`http://localhost:8080` in development), so no CORS configuration is needed.
2. The backend JWT is returned from the login endpoint as a JSON field named `token` in a
   body shaped as `{ token, expiresIn }`. The frontend will access `response.token`.
3. The backend serializes `BigDecimal` amounts as JSON numbers (Spring Boot default behavior).
   The frontend parses them with `parseFloat()` and formats with `toFixed(2)`.
4. All monetary amounts are displayed with a hardcoded `$` prefix. No currency field is
   exposed by the backend and no i18n is required for the initial release.
5. No i18n or localization is required beyond the `$` currency symbol. The UI is English-only.
6. No accessibility (WCAG) compliance level is formally required for the initial release,
   though semantic HTML (labels, fieldsets, buttons) MUST be used.
7. The frontend does not need to run offline or as a Progressive Web App (PWA).
8. The `isSystemDefault` field is present on category objects returned by the API, allowing
   the frontend to suppress the delete button for system categories.
9. Browser targets (current stable Chrome, Firefox, Edge) all support `fetch`, `async/await`,
   `localStorage`, ES modules (`type="module"`), and `<input type="date">` natively.
10. Pico.css and Chart.js are loaded from jsDelivr CDN. An internet connection is required
    during development and for end users. Bundling these locally is out of scope.

---

## Clarifications Received

The following questions were identified during specification drafting and resolved before
implementation begins:

| # | Area | Question | Answer |
|---|---|---|---|
| 1 | CSS approach | Is any lightweight single-file CSS library (e.g., Pico.css via CDN) permitted, or must all CSS be hand-written? | Use Pico.css via CDN — minimal, classless, no build step, makes the UI clean without hand-writing hundreds of CSS rules. |
| 2 | Application structure | Should the app be multiple HTML files (true MPA) or a single HTML file with hash-based view switching (SPA)? | Single `index.html` with JS-driven hash-based view switching (`#login`, `#dashboard`, `#transactions`, etc.) — simpler deployment, no server routing needed. |
| 3 | Post-registration flow | After successful registration, should the user be auto-logged-in (requires a follow-up login call) or redirected to the login page? | Auto-login — immediately POST to `/api/v1/auth/login` with same credentials after successful register, then redirect to dashboard. Better UX. |
| 4 | Dashboard budget widget | Is showing over-limit budgets on the dashboard a hard requirement or a nice-to-have? | Yes, firm requirement — show over-limit budgets on the dashboard. The extra API call is worth the visibility. |
| 5 | Category edit | Does the backend expose `PUT /api/v1/categories/{id}` for renaming? If not, should the UI omit the edit option? | Omit rename/edit — backend has no `PUT /categories/{id}`, so only create and delete custom categories. |
| 6 | Monthly trend chart | Is a visual chart on the trend page required, a soft goal, or out of scope? If required, is a CDN-hosted charting library permitted? | Yes, use Chart.js via CDN — it is a charting library not a framework, fits the "no build tools/no frameworks" constraint perfectly. |
| 7 | Mobile responsiveness | Is a responsive layout for viewports below 1024px required for the initial release? | Responsive layout required — Pico.css provides this mostly for free. |
| 8 | JWT storage | Should the JWT be stored in `localStorage` (persists across sessions) or `sessionStorage` (cleared on tab close)? | `localStorage` — persists across tabs and sessions, simpler implementation. |
| 9 | Frontend file location | Where should frontend files live: sibling directory, inside Spring Boot's `static/`, or a separate repo? | Inside `finance-tracker/src/main/resources/static/` — Spring Boot serves the files directly, zero CORS configuration needed. |
| 10 | BigDecimal serialization | Does the backend serialize `BigDecimal` as a JSON number or a JSON string? | JSON numbers (Spring Boot default) — parse with `parseFloat()`, format with `toFixed(2)`. |
| 11 | Currency symbol | Should a currency symbol be displayed in the UI? If so, which symbol, and should it be hardcoded or configurable? | Hardcoded `$` symbol — simple and functional for initial release. |

---

## Review & Acceptance Checklist

### Content Quality
- [x] All mandatory sections are present and fully filled
- [x] Requirements are written for developers and product stakeholders
- [x] All `[NEEDS CLARIFICATION]` markers resolved

### Requirement Completeness
- [x] No `[NEEDS CLARIFICATION]` markers remain
- [x] All functional requirements are testable and unambiguous
- [x] All in-scope pages and views are listed
- [x] API integration contract is documented
- [x] Error handling requirements are defined
- [x] Security requirements (JWT lifecycle) are specified
- [x] Scope is clearly bounded (plain HTML/CSS/JS + Pico.css CDN + Chart.js CDN, no frameworks, no build tools)
- [x] Assumptions are documented

### Foundation Alignment
- [x] Frontend consumes the existing REST API without modifying backend
- [x] JWT stateless authentication model respected
- [x] No server-side logic introduced by the frontend
- [x] Single currency assumption carried forward from backend spec
