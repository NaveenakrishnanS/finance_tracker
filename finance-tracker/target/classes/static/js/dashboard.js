/**
 * Finance Tracker — js/dashboard.js
 *
 * Dashboard view: shows current-month financial summary, category breakdown,
 * and a widget listing budgets that are currently over their limit.
 */

import { apiFetch, formatMoney, renderError } from './api.js';
import { redirectIfUnauth } from './auth.js';

/**
 * Initialise the dashboard view. Called by the router on every navigation to #dashboard.
 * @param {HTMLElement} container - The #view-dashboard section element.
 */
export function init(container) {
  if (redirectIfUnauth()) return;

  loadSummary();
  loadOverLimitBudgets();

  const filterForm = document.getElementById('summary-filter-form');
  filterForm.addEventListener('submit', (event) => {
    event.preventDefault();
    const startDate = document.getElementById('summary-start').value;
    const endDate = document.getElementById('summary-end').value;
    loadSummary(startDate, endDate);
  });
}

/**
 * Fetches the financial summary for the given date range (or current month if omitted)
 * and renders it into #summary-cards and #category-breakdown.
 * @param {string} [startDate]
 * @param {string} [endDate]
 */
async function loadSummary(startDate, endDate) {
  const summaryCards = document.getElementById('summary-cards');
  const categoryBreakdown = document.getElementById('category-breakdown');

  const params = new URLSearchParams();
  if (startDate) params.append('startDate', startDate);
  if (endDate) params.append('endDate', endDate);
  const query = params.toString() ? '?' + params.toString() : '';

  try {
    const data = await apiFetch('/api/v1/reports/summary' + query);

    summaryCards.innerHTML = `
      <div style="display:flex;gap:1rem;flex-wrap:wrap;">
        <article>
          <header>Total Income</header>
          <strong>${formatMoney(data.totalIncome)}</strong>
        </article>
        <article>
          <header>Total Expenses</header>
          <strong>${formatMoney(data.totalExpenses)}</strong>
        </article>
        <article>
          <header>Net Balance</header>
          <strong>${formatMoney(data.netBalance)}</strong>
        </article>
      </div>`;

    if (!data.categoryBreakdown || data.categoryBreakdown.length === 0) {
      categoryBreakdown.textContent = 'No category data for this period.';
    } else {
      let tableHtml = `
        <table>
          <thead><tr><th>Category</th><th>Income</th><th>Expenses</th></tr></thead>
          <tbody>`;
      for (const item of data.categoryBreakdown) {
        tableHtml += `<tr>
          <td>${item.categoryName}</td>
          <td>${formatMoney(item.income)}</td>
          <td>${formatMoney(item.expenses)}</td>
        </tr>`;
      }
      tableHtml += '</tbody></table>';
      categoryBreakdown.innerHTML = tableHtml;
    }
  } catch (err) {
    renderError(summaryCards, err);
  }
}

/**
 * Fetches all budgets and renders any that are over their limit into #over-limit-list.
 */
async function loadOverLimitBudgets() {
  const overLimitList = document.getElementById('over-limit-list');

  try {
    const budgets = await apiFetch('/api/v1/budgets');
    const overLimit = budgets.filter((b) => b.overLimit === true);

    if (overLimit.length === 0) {
      overLimitList.textContent = 'No budgets are currently over their limit.';
    } else {
      let html = '<ul>';
      for (const budget of overLimit) {
        html += `<li>
          <strong>${budget.categoryName}</strong> — 
          Limit: ${formatMoney(budget.limitAmount)}, 
          Spent: ${formatMoney(budget.spentAmount)}
          <a href="#budgets"> View Budgets</a>
        </li>`;
      }
      html += '</ul>';
      overLimitList.innerHTML = html;
    }
  } catch (err) {
    renderError(overLimitList, err);
  }
}
