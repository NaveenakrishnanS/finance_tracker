/**
 * Finance Tracker — js/router.js
 *
 * Hash-based single-page router. Listens for hashchange events, shows/hides
 * view sections, manages the nav bar visibility, and delegates to view init functions.
 * Login and Register handlers are implemented directly in this file.
 */

import { init as dashboardInit } from './dashboard.js';
import { init as transactionsInit } from './transactions.js';
import { init as budgetsInit } from './budgets.js';
import { init as goalsInit } from './goals.js';
import { init as categoriesInit } from './categories.js';
import { init as trendInit } from './trend.js';
import { init as profileInit } from './profile.js';
import { getToken, setToken, clearToken } from './auth.js';
import { apiFetch, setLoading, renderError } from './api.js';

// ─── Login Handler ─────────────────────────────────────────────────────────────

let loginListenerAttached = false;

function initLogin() {
  if (loginListenerAttached) return;
  loginListenerAttached = true;

  const form = document.getElementById('login-form');
  const loginError = document.getElementById('login-error');
  const loginBtn = document.getElementById('login-btn');

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    loginError.hidden = true;

    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    if (!email || !email.includes('@')) {
      renderError(loginError, { message: 'Please enter a valid email address.' });
      return;
    }
    if (!password) {
      renderError(loginError, { message: 'Password is required.' });
      return;
    }

    setLoading(loginBtn, true, 'Sign In');
    try {
      const data = await apiFetch('/api/v1/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });
      setToken(data.token);
      location.hash = '#dashboard';
    } catch (err) {
      renderError(loginError, err);
      setLoading(loginBtn, false, 'Sign In');
    }
  });
}

// ─── Register Handler ──────────────────────────────────────────────────────────

let registerListenerAttached = false;

function initRegister() {
  if (registerListenerAttached) return;
  registerListenerAttached = true;

  const form = document.getElementById('register-form');
  const regError = document.getElementById('reg-error');
  const regBtn = document.getElementById('reg-btn');

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    regError.hidden = true;

    const email = document.getElementById('reg-email').value.trim();
    const password = document.getElementById('reg-password').value;
    const displayName = document.getElementById('reg-name').value.trim();

    if (!email || !email.includes('@')) {
      renderError(regError, { message: 'Please enter a valid email address.' });
      return;
    }
    if (!password) {
      renderError(regError, { message: 'Password is required.' });
      return;
    }

    setLoading(regBtn, true, 'Create Account');
    try {
      await apiFetch('/api/v1/auth/register', {
        method: 'POST',
        body: JSON.stringify({ email, password, displayName }),
      });

      // Auto-login after successful registration (registration returns 201 but no token)
      const loginData = await apiFetch('/api/v1/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password }),
      });
      setToken(loginData.token);
      location.hash = '#dashboard';
    } catch (err) {
      renderError(regError, err);
      setLoading(regBtn, false, 'Create Account');
    }
  });
}

// ─── Route Map ─────────────────────────────────────────────────────────────────

const routes = {
  login:        { requiresAuth: false, init: initLogin },
  register:     { requiresAuth: false, init: initRegister },
  dashboard:    { requiresAuth: true,  init: dashboardInit },
  transactions: { requiresAuth: true,  init: transactionsInit },
  budgets:      { requiresAuth: true,  init: budgetsInit },
  goals:        { requiresAuth: true,  init: goalsInit },
  categories:   { requiresAuth: true,  init: categoriesInit },
  trend:        { requiresAuth: true,  init: trendInit },
  profile:      { requiresAuth: true,  init: profileInit },
};

// ─── Navigate ──────────────────────────────────────────────────────────────────

function navigate() {
  // Hide all view sections
  document.querySelectorAll('section.view').forEach((el) => el.classList.remove('active'));

  // Determine target hash
  const hash = (location.hash.replace('#', '') || 'login').toLowerCase();
  const route = routes[hash] || routes['login'];
  const routeName = routes[hash] ? hash : 'login';

  const token = getToken();

  // Guard: require auth
  if (route.requiresAuth && !token) {
    location.hash = '#login';
    return;
  }

  // Guard: already authenticated, don't show login/register
  if (!route.requiresAuth && token) {
    location.hash = '#dashboard';
    return;
  }

  // Show / hide navigation bar
  const nav = document.getElementById('app-nav');
  if (route.requiresAuth) {
    nav.classList.remove('hidden');
  } else {
    nav.classList.add('hidden');
  }

  // Show target section
  const section = document.getElementById('view-' + routeName);
  if (section) {
    section.classList.add('active');
  }

  // Call view init
  if (route.init) {
    route.init(section);
  }
}

// ─── Logout ────────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      clearToken();
      location.hash = '#login';
    });
  }
});

// ─── Event Listeners ───────────────────────────────────────────────────────────

window.addEventListener('hashchange', navigate);
document.addEventListener('DOMContentLoaded', navigate);
