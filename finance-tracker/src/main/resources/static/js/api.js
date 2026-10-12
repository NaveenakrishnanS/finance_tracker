/**
 * Finance Tracker — js/api.js
 *
 * Responsibilities: API_BASE constant, apiFetch() wrapper with automatic JWT attachment
 * and global 401 handling, and shared display-formatting helpers.
 *
 * No JWT or password value is ever logged to the browser console in this file.
 */

import { getToken, clearToken } from './auth.js';

/**
 * Base URL for all API calls.
 * Empty string means same-origin (frontend served by Spring Boot from src/main/resources/static/).
 * Change to 'http://localhost:8080' only when running the frontend from a separate origin
 * (e.g., a live-reload dev server on a different port).
 */
export const API_BASE = '';

/**
 * Guard flag to prevent duplicate 401 redirects when multiple concurrent requests fail
 * with an expired token at the same time.
 */
let redirectingToLogin = false;

/**
 * Centralized fetch wrapper. Attaches Authorization header, sets Content-Type for
 * requests with a body, handles 401 globally, and parses error responses.
 *
 * @param {string} path - API path (e.g. '/api/v1/transactions')
 * @param {RequestInit} [options={}] - Fetch options (method, body, etc.)
 * @returns {Promise<any>} Parsed JSON body for 2xx responses; null for 204 / empty body.
 * @throws {{ message: string, fieldErrors?: Array }} For 4xx/5xx responses.
 */
export async function apiFetch(path, options = {}) {
  const token = getToken();

  const headers = { ...(options.headers || {}) };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  if (options.body) {
    headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(API_BASE + path, { ...options, headers });

  if (response.status === 401) {
    if (!redirectingToLogin) {
      redirectingToLogin = true;
      clearToken();
      location.hash = '#login';
    }
    throw { message: 'Session expired. Please log in again.' };
  }

  if (response.status >= 400) {
    let errBody;
    try {
      errBody = await response.json();
    } catch {
      throw { message: 'An unexpected error occurred.' };
    }
    throw {
      message: errBody.message || 'An unexpected error occurred.',
      fieldErrors: errBody.fieldErrors || null,
    };
  }

  if (response.status === 204 || response.headers.get('content-length') === '0') {
    return null;
  }

  const text = await response.text();
  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

/**
 * Formats a numeric monetary value as a USD string with $ prefix and two decimal places.
 * Example: formatMoney(1234.5) → '$1,234.50'
 * @param {number|string} value
 * @returns {string}
 */
export function formatMoney(value) {
  return '$' + parseFloat(value).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

/**
 * Formats an ISO 8601 date string (YYYY-MM-DD) for locale-friendly display.
 * Uses local-time constructor to avoid UTC off-by-one issues.
 * @param {string} isoDate - e.g. '2026-03-09'
 * @returns {string}
 */
export function formatDate(isoDate) {
  const [year, month, day] = isoDate.split('-').map(Number);
  return new Date(year, month - 1, day).toLocaleDateString();
}

/**
 * Sets a button into loading or default state.
 * Disables the button and shows 'Loading...' while a request is in flight.
 * @param {HTMLButtonElement} btn
 * @param {boolean} loading
 * @param {string} defaultLabel
 */
export function setLoading(btn, loading, defaultLabel) {
  btn.disabled = loading;
  btn.textContent = loading ? 'Loading...' : defaultLabel;
}

/**
 * Renders an error message (or list of field errors) into an error container element.
 * Shows the element and populates it with the error details.
 * @param {HTMLElement} errorEl - Container element (should have class 'error-msg')
 * @param {{ message?: string, fieldErrors?: Array<{field: string, message: string}> }} err
 */
export function renderError(errorEl, err) {
  errorEl.hidden = false;
  if (err.fieldErrors && Array.isArray(err.fieldErrors) && err.fieldErrors.length > 0) {
    const ul = document.createElement('ul');
    for (const fe of err.fieldErrors) {
      const li = document.createElement('li');
      li.textContent = `${fe.field}: ${fe.message}`;
      ul.appendChild(li);
    }
    errorEl.innerHTML = '';
    errorEl.appendChild(ul);
  } else {
    errorEl.textContent = err.message || 'An unexpected error occurred.';
  }
}
