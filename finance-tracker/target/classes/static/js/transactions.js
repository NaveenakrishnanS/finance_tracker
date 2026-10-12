/**
 * Finance Tracker — js/transactions.js
 *
 * Transactions view: paginated list with filters, add/edit/delete form.
 */

import { apiFetch, formatMoney, formatDate, renderError, setLoading } from './api.js';
import { redirectIfUnauth } from './auth.js';
import { getCategories } from './categories.js';

let currentPage = 0;
const PAGE_SIZE = 20;
let currentFilters = {};
let currentEditId = null;
let listenersAttached = false;

/**
 * Initialise the transactions view.
 * @param {HTMLElement} container
 */
export async function init(container) {
  if (redirectIfUnauth()) return;

  try {
    const categories = await getCategories();
    populateCategoryDropdown(categories);
  } catch (err) {
    // Non-fatal: category dropdowns remain empty
  }

  await loadTransactions();

  if (listenersAttached) return;
  listenersAttached = true;

  document.getElementById('tx-form').addEventListener('submit', (e) => {
    e.preventDefault();
    handleFormSubmit();
  });

  document.getElementById('tx-filter-form').addEventListener('submit', (e) => {
    e.preventDefault();
    currentPage = 0;
    currentFilters = extractFilters();
    loadTransactions();
  });

  document.getElementById('filter-reset').addEventListener('click', () => {
    currentFilters = {};
    currentPage = 0;
    loadTransactions();
  });

  document.getElementById('tx-prev').addEventListener('click', () => {
    currentPage--;
    loadTransactions();
  });

  document.getElementById('tx-next').addEventListener('click', () => {
    currentPage++;
    loadTransactions();
  });

  document.getElementById('tx-cancel').addEventListener('click', () => {
    resetForm();
  });
}

function extractFilters() {
  return {
    type: document.getElementById('filter-type').value || null,
    categoryId: document.getElementById('filter-category').value || null,
    startDate: document.getElementById('filter-start').value || null,
    endDate: document.getElementById('filter-end').value || null,
  };
}

function populateCategoryDropdown(categories) {
  const txCategory = document.getElementById('tx-category');
  const filterCategory = document.getElementById('filter-category');

  txCategory.innerHTML = '';
  for (const cat of categories) {
    const opt = document.createElement('option');
    opt.value = cat.id;
    opt.textContent = cat.name;
    txCategory.appendChild(opt);
  }

  filterCategory.innerHTML = '<option value="">All Categories</option>';
  for (const cat of categories) {
    const opt = document.createElement('option');
    opt.value = cat.id;
    opt.textContent = cat.name;
    filterCategory.appendChild(opt);
  }
}

async function loadTransactions() {
  const txList = document.getElementById('tx-list');
  const txPagination = document.getElementById('tx-pagination');

  const params = new URLSearchParams({ page: currentPage, size: PAGE_SIZE });
  if (currentFilters.type) params.append('type', currentFilters.type);
  if (currentFilters.categoryId) params.append('categoryId', currentFilters.categoryId);
  if (currentFilters.startDate) params.append('startDate', currentFilters.startDate);
  if (currentFilters.endDate) params.append('endDate', currentFilters.endDate);

  try {
    const data = await apiFetch('/api/v1/transactions?' + params.toString());

    if (!data.content || data.content.length === 0) {
      txList.innerHTML = '<p>No transactions found.</p>';
      txPagination.hidden = true;
      return;
    }

    txPagination.hidden = false;

    const rows = data.content.map((tx) => {
      const rawDesc = tx.description || '';
      const desc = rawDesc.length > 50 ? rawDesc.substring(0, 50) + '\u2026' : rawDesc;
      return `<tr>
        <td>${formatDate(tx.date)}</td>
        <td>${tx.type}</td>
        <td>${tx.categoryName || ''}</td>
        <td>${formatMoney(tx.amount)}</td>
        <td>${desc}</td>
        <td>
          <button type="button" onclick="window._editTransaction(${tx.id})">Edit</button>
          <button type="button" onclick="window._deleteTransaction(${tx.id})">Delete</button>
        </td>
      </tr>`;
    }).join('');

    txList.innerHTML = `
      <table>
        <thead><tr>
          <th>Date</th><th>Type</th><th>Category</th>
          <th>Amount</th><th>Description</th><th>Actions</th>
        </tr></thead>
        <tbody>${rows}</tbody>
      </table>`;

    const prevBtn = document.getElementById('tx-prev');
    const nextBtn = document.getElementById('tx-next');
    const pageInfo = document.getElementById('tx-page-info');

    prevBtn.disabled = data.first === true;
    nextBtn.disabled = data.last === true;
    const total = data.totalPages || 0;
    pageInfo.textContent = total === 0
      ? 'No results'
      : `Page ${data.number + 1} of ${total}`;
  } catch (err) {
    renderError(txList, err);
  }
}

async function handleFormSubmit() {
  const txError = document.getElementById('tx-error');
  const txBtn = document.getElementById('tx-btn');
  txError.hidden = true;

  const amount = parseFloat(document.getElementById('tx-amount').value);
  const type = document.getElementById('tx-type').value;
  const categoryId = document.getElementById('tx-category').value;
  const date = document.getElementById('tx-date').value;
  const description = document.getElementById('tx-desc').value.trim();

  if (!amount || amount <= 0) {
    renderError(txError, { message: 'Amount must be greater than 0.' });
    return;
  }
  if (!type) {
    renderError(txError, { message: 'Transaction type is required.' });
    return;
  }
  if (!categoryId) {
    renderError(txError, { message: 'Category is required.' });
    return;
  }
  if (!date) {
    renderError(txError, { message: 'Date is required.' });
    return;
  }

  const label = currentEditId ? 'Update Transaction' : 'Add Transaction';
  setLoading(txBtn, true, label);

  const body = JSON.stringify({ amount, type, categoryId: Number(categoryId), date, description });

  try {
    if (currentEditId === null) {
      await apiFetch('/api/v1/transactions', { method: 'POST', body });
    } else {
      await apiFetch('/api/v1/transactions/' + currentEditId, { method: 'PUT', body });
    }
    resetForm();
    await loadTransactions();
  } catch (err) {
    renderError(txError, err);
    setLoading(txBtn, false, label);
  }
}

async function editTransaction(id) {
  try {
    const tx = await apiFetch('/api/v1/transactions/' + id);
    document.getElementById('tx-id').value = tx.id;
    document.getElementById('tx-amount').value = tx.amount;
    document.getElementById('tx-type').value = tx.type;
    document.getElementById('tx-category').value = tx.categoryId;
    document.getElementById('tx-date').value = tx.date;
    document.getElementById('tx-desc').value = tx.description || '';
    currentEditId = id;

    document.getElementById('tx-form-toggle').textContent = 'Edit Transaction';
    document.getElementById('tx-btn').textContent = 'Update Transaction';
    document.getElementById('tx-cancel').hidden = false;
    document.getElementById('tx-form-details').open = true;
  } catch (err) {
    const txError = document.getElementById('tx-error');
    renderError(txError, err);
  }
}

async function deleteTransaction(id) {
  if (!confirm('Delete this transaction?')) return;
  try {
    await apiFetch('/api/v1/transactions/' + id, { method: 'DELETE' });
    await loadTransactions();
  } catch (err) {
    const txList = document.getElementById('tx-list');
    renderError(txList, err);
  }
}

function resetForm() {
  document.getElementById('tx-form').reset();
  document.getElementById('tx-id').value = '';
  currentEditId = null;
  document.getElementById('tx-form-toggle').textContent = 'Add Transaction';
  document.getElementById('tx-btn').textContent = 'Add Transaction';
  document.getElementById('tx-btn').disabled = false;
  document.getElementById('tx-cancel').hidden = true;
  document.getElementById('tx-error').hidden = true;
}

// Expose functions as globals for inline onclick handlers in the rendered table
window._editTransaction = editTransaction;
window._deleteTransaction = deleteTransaction;
