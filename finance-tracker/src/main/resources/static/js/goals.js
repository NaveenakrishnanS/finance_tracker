/**
 * Finance Tracker — js/goals.js
 *
 * Goals view: list goals with progress tracking, add/edit/delete form,
 * and inline contribution sub-form per goal.
 */

import { apiFetch, formatMoney, formatDate, renderError, setLoading } from './api.js';
import { redirectIfUnauth } from './auth.js';

let currentEditId = null;
let listenersAttached = false;

/**
 * Initialise the goals view.
 * @param {HTMLElement} container
 */
export function init(container) {
  if (redirectIfUnauth()) return;

  loadGoals();

  if (listenersAttached) return;
  listenersAttached = true;

  document.getElementById('goal-form').addEventListener('submit', (e) => {
    e.preventDefault();
    handleGoalSubmit();
  });

  document.getElementById('goal-cancel').addEventListener('click', () => {
    resetGoalForm();
  });

  // Event delegation on #goal-list for contribution form submits
  document.getElementById('goal-list').addEventListener('submit', (e) => {
    if (e.target.classList.contains('contribution-form')) {
      e.preventDefault();
      const goalId = e.target.dataset.goalId;
      const amountInput = e.target.querySelector('.contrib-amount');
      const contribErrorEl = e.target.querySelector('.contrib-error');
      const amount = parseFloat(amountInput.value);
      if (!amount || amount <= 0) {
        contribErrorEl.hidden = false;
        contribErrorEl.textContent = 'Contribution amount must be greater than 0.';
        return;
      }
      handleContribution(goalId, amount, contribErrorEl);
    }
  });
}

async function loadGoals() {
  const goalList = document.getElementById('goal-list');

  try {
    const goals = await apiFetch('/api/v1/goals');

    if (!goals || goals.length === 0) {
      goalList.innerHTML = '<p>No goals defined yet.</p>';
      return;
    }

    const rows = goals.map((goal) => {
      const remaining = parseFloat(goal.targetAmount) - parseFloat(goal.contributedAmount);
      const progress = goal.progressPercentage != null ? goal.progressPercentage : 0;
      const achievedClass = goal.status === 'ACHIEVED' ? ' class="achieved"' : '';
      const statusBadge = goal.status === 'ACHIEVED'
        ? '<span class="achieved-badge">Achieved</span>'
        : goal.status;
      const targetDateDisplay = goal.targetDate ? formatDate(goal.targetDate) : '';

      return `<tr${achievedClass}>
        <td>${goal.name}</td>
        <td>${formatMoney(goal.targetAmount)}</td>
        <td>${formatMoney(goal.contributedAmount)}</td>
        <td>${formatMoney(remaining)}</td>
        <td>${progress}%</td>
        <td>${targetDateDisplay}</td>
        <td>${statusBadge}</td>
        <td>
          <form class="contribution-form" data-goal-id="${goal.id}" style="display:inline-flex;gap:0.25rem;align-items:center;">
            <input type="number" class="contrib-amount" min="0.01" step="0.01" placeholder="Amount" required style="width:100px;">
            <button type="submit">Add Contribution</button>
            <span class="contrib-error error-msg" hidden></span>
          </form>
        </td>
        <td>
          <button type="button" onclick="window._editGoal(${JSON.stringify(JSON.stringify(goal))})">Edit</button>
          <button type="button" onclick="window._deleteGoal(${goal.id})">Delete</button>
        </td>
      </tr>`;
    }).join('');

    goalList.innerHTML = `
      <table>
        <thead><tr>
          <th>Name</th><th>Target</th><th>Contributed</th><th>Remaining</th>
          <th>Progress</th><th>Target Date</th><th>Status</th>
          <th>Contribute</th><th>Actions</th>
        </tr></thead>
        <tbody>${rows}</tbody>
      </table>`;
  } catch (err) {
    renderError(goalList, err);
  }
}

async function handleGoalSubmit() {
  const goalError = document.getElementById('goal-error');
  const goalBtn = document.getElementById('goal-btn');
  goalError.hidden = true;

  const name = document.getElementById('goal-name').value.trim();
  const targetAmount = parseFloat(document.getElementById('goal-target').value);
  const targetDate = document.getElementById('goal-date').value;

  if (!name) {
    renderError(goalError, { message: 'Goal name is required.' });
    return;
  }
  if (!targetAmount || targetAmount <= 0) {
    renderError(goalError, { message: 'Target amount must be greater than 0.' });
    return;
  }
  if (!targetDate) {
    renderError(goalError, { message: 'Target date is required.' });
    return;
  }

  const label = currentEditId ? 'Update Goal' : 'Add Goal';
  setLoading(goalBtn, true, label);

  try {
    if (currentEditId === null) {
      await apiFetch('/api/v1/goals', {
        method: 'POST',
        body: JSON.stringify({ name, targetAmount, targetDate }),
      });
    } else {
      await apiFetch('/api/v1/goals/' + currentEditId, {
        method: 'PUT',
        body: JSON.stringify({ name, targetAmount, targetDate }),
      });
    }
    resetGoalForm();
    await loadGoals();
  } catch (err) {
    renderError(goalError, err);
    setLoading(goalBtn, false, label);
  }
}

async function handleContribution(goalId, amount, contribErrorEl) {
  try {
    await apiFetch('/api/v1/goals/' + goalId + '/contributions', {
      method: 'POST',
      body: JSON.stringify({ amount }),
    });
    await loadGoals();
  } catch (err) {
    contribErrorEl.hidden = false;
    contribErrorEl.textContent = err.message || 'An unexpected error occurred.';
  }
}

function editGoal(goal) {
  currentEditId = goal.id;
  document.getElementById('goal-id').value = goal.id;
  document.getElementById('goal-name').value = goal.name;
  document.getElementById('goal-target').value = goal.targetAmount;
  document.getElementById('goal-date').value = goal.targetDate || '';

  document.getElementById('goal-btn').textContent = 'Update Goal';
  document.getElementById('goal-cancel').hidden = false;
}

async function deleteGoal(id) {
  if (!confirm('Delete this goal?')) return;
  try {
    await apiFetch('/api/v1/goals/' + id, { method: 'DELETE' });
    await loadGoals();
  } catch (err) {
    const goalList = document.getElementById('goal-list');
    renderError(goalList, err);
  }
}

function resetGoalForm() {
  document.getElementById('goal-form').reset();
  document.getElementById('goal-id').value = '';
  currentEditId = null;
  document.getElementById('goal-btn').textContent = 'Add Goal';
  document.getElementById('goal-btn').disabled = false;
  document.getElementById('goal-cancel').hidden = true;
  document.getElementById('goal-error').hidden = true;
}

// Expose as globals for inline onclick handlers
window._editGoal = (goalJson) => editGoal(JSON.parse(goalJson));
window._deleteGoal = deleteGoal;
