/**
 * Finance Tracker — js/budgets.js
 *
 * Budgets view: list all budgets with spent/remaining/over-limit display,
 * add/edit/delete form. Only the limit amount is editable on update (PUT).
 */

import { apiFetch, formatMoney, renderError, setLoading } from './api.js';
import { redirectIfUnauth } from './auth.js';
import { getCategories } from './categories.js';

let currentEditId = null;
let listenersAttached = false;

/**
 * Initialise the budgets view.
 * @param {HTMLElement} container
 */
export async function init(container) {
  if (redirectIfUnauth()) return;

  try {
    const categories = await getCategories();
    const select = document.getElementById('budget-category');
    select.innerHTML = '';
    for (const cat of categories) {
      const opt = document.createElement('option');
      opt.value = cat.id;
      opt.textContent = cat.name;
      select.appendChild(opt);
    }
  } catch (err) {
    // Non-fatal
  }

  await loadBudgets();

  if (listenersAttached) return;
  listenersAttached = true;

  document.getElementById('budget-form').addEventListener('submit', (e) => {
    e.preventDefault();
    handleBudgetSubmit();
  });

  document.getElementById('budget-cancel').addEventListener('click', () => {
    resetBudgetForm();
  });
}

async function loadBudgets() {
  const budgetList = document.getElementById('budget-list');

  try {
    const budgets = await apiFetch('/api/v1/budgets');

    if (!budgets || budgets.length === 0) {
      budgetList.innerHTML = '<p>No budgets defined yet.</p>';
      return;
    }

    const rows = budgets.map((budget) => {
      const remaining = parseFloat(budget.limitAmount) - parseFloat(budget.spentAmount);
      const overLimitClass = budget.overLimit ? ' class="over-limit"' : '';
      const statusCell = budget.overLimit
        ? '<span class="over-limit-badge">Over Limit</span>'
        : 'OK';
      return `<tr${overLimitClass}>
        <td>${budget.categoryName || ''}</td>
        <td>${budget.month}/${budget.year}</td>
        <td>${formatMoney(budget.limitAmount)}</td>
        <td>${formatMoney(budget.spentAmount)}</td>
        <td>${formatMoney(remaining)}</td>
        <td>${statusCell}</td>
        <td>
          <button type="button" onclick="window._editBudget(${JSON.stringify(JSON.stringify(budget))})">Edit Limit</button>
          <button type="button" onclick="window._deleteBudget(${budget.id})">Delete</button>
        </td>
      </tr>`;
    }).join('');

    budgetList.innerHTML = `
      <table>
        <thead><tr>
          <th>Category</th><th>Month/Year</th><th>Limit</th>
          <th>Spent</th><th>Remaining</th><th>Status</th><th>Actions</th>
        </tr></thead>
        <tbody>${rows}</tbody>
      </table>`;
  } catch (err) {
    renderError(budgetList, err);
  }
}

async function handleBudgetSubmit() {
  const budgetError = document.getElementById('budget-error');
  const budgetBtn = document.getElementById('budget-btn');
  budgetError.hidden = true;

  const categoryId = document.getElementById('budget-category').value;
  const limitAmount = parseFloat(document.getElementById('budget-limit').value);
  const month = parseInt(document.getElementById('budget-month').value, 10);
  const year = parseInt(document.getElementById('budget-year').value, 10);

  if (!categoryId) {
    renderError(budgetError, { message: 'Category is required.' });
    return;
  }
  if (!limitAmount || limitAmount <= 0) {
    renderError(budgetError, { message: 'Limit amount must be greater than 0.' });
    return;
  }
  if (!month || month < 1 || month > 12) {
    renderError(budgetError, { message: 'Month must be between 1 and 12.' });
    return;
  }
  if (!year) {
    renderError(budgetError, { message: 'Year is required.' });
    return;
  }

  const label = currentEditId ? 'Update Budget' : 'Add Budget';
  setLoading(budgetBtn, true, label);

  try {
    if (currentEditId === null) {
      await apiFetch('/api/v1/budgets', {
        method: 'POST',
        body: JSON.stringify({ categoryId: Number(categoryId), limitAmount, month, year }),
      });
    } else {
      await apiFetch('/api/v1/budgets/' + currentEditId, {
        method: 'PUT',
        body: JSON.stringify({ limitAmount }),
      });
    }
    resetBudgetForm();
    await loadBudgets();
  } catch (err) {
    renderError(budgetError, err);
    setLoading(budgetBtn, false, label);
  }
}

function editBudget(budget) {
  currentEditId = budget.id;
  document.getElementById('budget-id').value = budget.id;
  document.getElementById('budget-limit').value = budget.limitAmount;

  // Lock fields that cannot be changed via PUT (only limit is editable)
  document.getElementById('budget-category').disabled = true;
  document.getElementById('budget-month').disabled = true;
  document.getElementById('budget-year').disabled = true;

  document.getElementById('budget-btn').textContent = 'Update Budget';
  document.getElementById('budget-cancel').hidden = false;
}

async function deleteBudget(id) {
  if (!confirm('Delete this budget?')) return;
  try {
    await apiFetch('/api/v1/budgets/' + id, { method: 'DELETE' });
    await loadBudgets();
  } catch (err) {
    const budgetList = document.getElementById('budget-list');
    renderError(budgetList, err);
  }
}

function resetBudgetForm() {
  document.getElementById('budget-form').reset();
  document.getElementById('budget-id').value = '';
  currentEditId = null;
  document.getElementById('budget-category').disabled = false;
  document.getElementById('budget-month').disabled = false;
  document.getElementById('budget-year').disabled = false;
  document.getElementById('budget-btn').textContent = 'Add Budget';
  document.getElementById('budget-btn').disabled = false;
  document.getElementById('budget-cancel').hidden = true;
  document.getElementById('budget-error').hidden = true;
}

// Expose as globals for inline onclick handlers
window._editBudget = (budgetJson) => editBudget(JSON.parse(budgetJson));
window._deleteBudget = deleteBudget;
