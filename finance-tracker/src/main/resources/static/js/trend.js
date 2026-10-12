/**
 * Finance Tracker — js/trend.js
 *
 * Monthly Trend view: fetches monthly income/expense/net data, renders a table
 * and a Chart.js bar chart. Destroys and re-creates the chart on each filter
 * application to avoid canvas reuse errors.
 *
 * TODO: Confirm the API response field name for the month-year label
 * (e.g. 'monthYear' vs individual 'year' and 'month' fields) on first integration
 * with the backend. The current implementation checks for both shapes.
 */

import { apiFetch, formatMoney, renderError } from './api.js';
import { redirectIfUnauth } from './auth.js';

/** Holds the current Chart.js instance so it can be destroyed before re-creation. */
let trendChartInstance = null;
let listenersAttached = false;

/**
 * Initialise the monthly trend view.
 * @param {HTMLElement} container
 */
export function init(container) {
  if (redirectIfUnauth()) return;

  loadTrend();

  if (listenersAttached) return;
  listenersAttached = true;

  document.getElementById('trend-filter-form').addEventListener('submit', (e) => {
    e.preventDefault();
    const startDate = document.getElementById('trend-start').value;
    const endDate = document.getElementById('trend-end').value;
    loadTrend(startDate, endDate);
  });
}

/**
 * Fetches monthly trend data and renders the table and bar chart.
 * @param {string} [startDate]
 * @param {string} [endDate]
 */
async function loadTrend(startDate, endDate) {
  const trendError = document.getElementById('trend-error');
  const trendTable = document.getElementById('trend-table');
  trendError.hidden = true;

  const params = new URLSearchParams();
  if (startDate) params.append('startDate', startDate);
  if (endDate) params.append('endDate', endDate);
  const query = params.toString() ? '?' + params.toString() : '';

  try {
    const data = await apiFetch('/api/v1/reports/monthly-trend' + query);

    if (!data || data.length === 0) {
      trendTable.innerHTML = '<p>No trend data for this period.</p>';
      destroyChart();
      return;
    }

    // Render table
    const rows = data.map((item) => {
      // Support both 'monthYear' string and separate 'year'/'month' fields
      const label = item.monthYear
        ? item.monthYear
        : `${item.year}-${String(item.month).padStart(2, '0')}`;
      return `<tr>
        <td>${label}</td>
        <td>${formatMoney(item.totalIncome)}</td>
        <td>${formatMoney(item.totalExpenses)}</td>
        <td>${formatMoney(item.netBalance)}</td>
      </tr>`;
    }).join('');

    trendTable.innerHTML = `
      <table>
        <thead><tr>
          <th>Month/Year</th><th>Income</th><th>Expenses</th><th>Net Balance</th>
        </tr></thead>
        <tbody>${rows}</tbody>
      </table>`;

    // Build chart data
    const labels = data.map((item) =>
      item.monthYear
        ? item.monthYear
        : `${item.year}-${String(item.month).padStart(2, '0')}`
    );
    const incomes = data.map((item) => parseFloat(item.totalIncome));
    const expenses = data.map((item) => parseFloat(item.totalExpenses));
    const nets = data.map((item) => parseFloat(item.netBalance));

    // Destroy existing chart before creating a new one (avoids canvas reuse errors)
    destroyChart();

    const canvas = document.getElementById('trendChart');
    trendChartInstance = new Chart(canvas, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          {
            label: 'Income',
            data: incomes,
            backgroundColor: 'rgba(56,161,105,0.7)',
          },
          {
            label: 'Expenses',
            data: expenses,
            backgroundColor: 'rgba(229,62,62,0.7)',
          },
          {
            label: 'Net Balance',
            data: nets,
            backgroundColor: 'rgba(66,153,225,0.7)',
          },
        ],
      },
      options: {
        responsive: true,
        scales: {
          y: { beginAtZero: true },
        },
      },
    });
  } catch (err) {
    renderError(trendError, err);
  }
}

function destroyChart() {
  if (trendChartInstance) {
    trendChartInstance.destroy();
    trendChartInstance = null;
  }
}
