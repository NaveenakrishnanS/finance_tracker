/**
 * Finance Tracker — js/profile.js
 *
 * Profile / Account view: display user info, update display name,
 * change password, and delete account.
 */

import { apiFetch, renderError, setLoading } from './api.js';
import { redirectIfUnauth, clearToken } from './auth.js';

let listenersAttached = false;

/**
 * Initialise the profile view.
 * @param {HTMLElement} container
 */
export function init(container) {
  if (redirectIfUnauth()) return;

  loadProfile();

  if (listenersAttached) return;
  listenersAttached = true;

  document.getElementById('name-form').addEventListener('submit', (e) => {
    e.preventDefault();
    handleNameUpdate();
  });

  document.getElementById('password-form').addEventListener('submit', (e) => {
    e.preventDefault();
    handlePasswordChange();
  });

  document.getElementById('delete-account-btn').addEventListener('click', () => {
    handleDeleteAccount();
  });
}

async function loadProfile() {
  const profileInfo = document.getElementById('profile-info');

  try {
    const user = await apiFetch('/api/v1/users/me');
    profileInfo.innerHTML = `
      <p><strong>Email:</strong> ${user.email}</p>
      <p><strong>Display Name:</strong> ${user.displayName || '\u2014'}</p>`;
    document.getElementById('display-name').value = user.displayName || '';
  } catch (err) {
    renderError(profileInfo, err);
  }
}

async function handleNameUpdate() {
  const nameMsg = document.getElementById('name-msg');
  const nameBtn = document.getElementById('name-btn');
  nameMsg.hidden = true;
  nameMsg.className = '';

  const displayName = document.getElementById('display-name').value.trim();

  setLoading(nameBtn, true, 'Update');
  try {
    await apiFetch('/api/v1/users/me', {
      method: 'PUT',
      body: JSON.stringify({ displayName }),
    });
    nameMsg.hidden = false;
    nameMsg.className = 'success-msg';
    nameMsg.textContent = 'Display name updated.';
    setLoading(nameBtn, false, 'Update');
  } catch (err) {
    renderError(nameMsg, err);
    nameMsg.hidden = false;
    setLoading(nameBtn, false, 'Update');
  }
}

async function handlePasswordChange() {
  const passwordMsg = document.getElementById('password-msg');
  const passwordBtn = document.getElementById('password-btn');
  passwordMsg.hidden = true;
  passwordMsg.className = '';

  const currentPassword = document.getElementById('current-password').value;
  const newPassword = document.getElementById('new-password').value;

  if (!currentPassword) {
    renderError(passwordMsg, { message: 'Current password is required.' });
    passwordMsg.hidden = false;
    return;
  }
  if (!newPassword) {
    renderError(passwordMsg, { message: 'New password is required.' });
    passwordMsg.hidden = false;
    return;
  }

  setLoading(passwordBtn, true, 'Change Password');
  try {
    await apiFetch('/api/v1/users/me/password', {
      method: 'PUT',
      body: JSON.stringify({ currentPassword, newPassword }),
    });
    document.getElementById('password-form').reset();
    passwordMsg.hidden = false;
    passwordMsg.className = 'success-msg';
    passwordMsg.textContent = 'Password changed successfully.';
    setLoading(passwordBtn, false, 'Change Password');
  } catch (err) {
    renderError(passwordMsg, err);
    passwordMsg.hidden = false;
    setLoading(passwordBtn, false, 'Change Password');
  }
}

async function handleDeleteAccount() {
  const deleteMsg = document.getElementById('delete-msg');

  if (!confirm('Are you sure you want to permanently delete your account? This cannot be undone.')) {
    return;
  }

  try {
    await apiFetch('/api/v1/users/me', { method: 'DELETE' });
    clearToken();
    location.hash = '#login';
  } catch (err) {
    deleteMsg.hidden = false;
    deleteMsg.textContent = err.message || 'An unexpected error occurred.';
  }
}
