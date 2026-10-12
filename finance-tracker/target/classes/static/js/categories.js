/**
 * Finance Tracker — js/categories.js
 *
 * Manages the Categories view: list all categories, add a custom category,
 * delete a custom category (with conflict handling). Provides getCategories()
 * for other views to use without refetching on every navigation.
 */

import { apiFetch, renderError, setLoading } from './api.js';
import { redirectIfUnauth } from './auth.js';

/**
 * Module-level category cache. Populated on first fetch; invalidated on mutations.
 * Cache is invalidated on mutation so that other views calling getCategories()
 * receive fresh data on next navigation.
 * @type {Array|null}
 */
let categoriesCache = null;

/**
 * Initialise the categories view.
 * @param {HTMLElement} container
 */
export function init(container) {
  if (redirectIfUnauth()) return;

  // Invalidate cache on view entry so list is always fresh when user navigates here
  categoriesCache = null;
  loadCategories();

  const form = document.getElementById('category-form');
  form.addEventListener('submit', (event) => {
    event.preventDefault();
    handleAddCategory();
  });
}

/**
 * Returns the cached category list, fetching from the API if the cache is empty.
 * @returns {Promise<Array>}
 */
export async function getCategories() {
  if (categoriesCache !== null) return categoriesCache;
  categoriesCache = await apiFetch('/api/v1/categories');
  return categoriesCache;
}

/**
 * Fetches and renders the full category list into #cat-list.
 * Uses the cache when available (satisfies NFR-041).
 */
async function loadCategories() {
  const catList = document.getElementById('cat-list');

  try {
    const categories = await getCategories();

    if (categories.length === 0) {
      catList.textContent = 'No categories found.';
      return;
    }

    let html = `
      <table>
        <thead><tr><th>Name</th><th>Type</th><th>Actions</th></tr></thead>
        <tbody>`;
    for (const cat of categories) {
      const type = cat.isSystemDefault ? 'System' : 'Custom';
      const deleteBtn = cat.isSystemDefault
        ? ''
        : `<button type="button" onclick="window._deleteCategory(${cat.id})">Delete</button>`;
      html += `<tr>
        <td>${cat.name}</td>
        <td>${type}</td>
        <td>${deleteBtn}</td>
      </tr>`;
    }
    html += '</tbody></table>';
    catList.innerHTML = html;
  } catch (err) {
    renderError(catList, err);
  }
}

/**
 * Handles the "Add Category" form submission.
 */
async function handleAddCategory() {
  const catNameInput = document.getElementById('cat-name');
  const catError = document.getElementById('cat-error');
  const catBtn = document.getElementById('cat-btn');

  catError.hidden = true;

  const name = catNameInput.value.trim();
  if (!name) {
    renderError(catError, { message: 'Category name is required.' });
    return;
  }

  setLoading(catBtn, true, 'Add Category');
  try {
    await apiFetch('/api/v1/categories', {
      method: 'POST',
      body: JSON.stringify({ name }),
    });
    catNameInput.value = '';
    // Cache is invalidated on mutation so that other views calling getCategories()
    // receive fresh data on next navigation.
    categoriesCache = null;
    await loadCategories();
    setLoading(catBtn, false, 'Add Category');
  } catch (err) {
    renderError(catError, err);
    setLoading(catBtn, false, 'Add Category');
  }
}

/**
 * Deletes a category by ID after confirmation.
 * Shows a friendly message for 409 Conflict (category in use).
 * @param {number} id
 */
async function deleteCategory(id) {
  const catError = document.getElementById('cat-error');
  catError.hidden = true;

  if (!confirm('Delete this category?')) return;

  try {
    await apiFetch('/api/v1/categories/' + id, { method: 'DELETE' });
    // Cache is invalidated on mutation so that other views calling getCategories()
    // receive fresh data on next navigation.
    categoriesCache = null;
    await loadCategories();
  } catch (err) {
    const message =
      (err.message && err.message.toLowerCase().includes('conflict')) ||
      err.status === 409
        ? 'This category is in use by existing transactions or budgets and cannot be deleted.'
        : err.message || 'An unexpected error occurred.';
    renderError(catError, { message });
  }
}

// Expose deleteCategory as a global so inline onclick handlers in the table can call it.
// This avoids complex event-delegation wiring while keeping the logic in this module.
window._deleteCategory = deleteCategory;
