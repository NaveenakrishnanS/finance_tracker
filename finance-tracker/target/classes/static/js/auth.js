/**
 * Finance Tracker — js/auth.js
 *
 * Responsibilities: JWT token storage and auth-redirect helpers only.
 * No business logic belongs in this file beyond the five functions listed below.
 * No JWT value is ever logged to the browser console.
 */

const TOKEN_KEY = 'ft_token';

/**
 * Returns the stored JWT access token, or null if not present.
 * @returns {string|null}
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

/**
 * Stores the JWT access token in localStorage.
 * @param {string} token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
}

/**
 * Removes the JWT access token from localStorage.
 */
export function clearToken() {
  localStorage.removeItem(TOKEN_KEY);
}

/**
 * Redirects to the login view if no token is present.
 * @returns {boolean} true if redirected (unauthenticated), false if authenticated.
 */
export function redirectIfUnauth() {
  if (getToken() === null) {
    location.hash = '#login';
    return true;
  }
  return false;
}

/**
 * Redirects to the dashboard view if a token is already present.
 * @returns {boolean} true if redirected (already authenticated), false if not authenticated.
 */
export function redirectIfAuth() {
  if (getToken() !== null) {
    location.hash = '#dashboard';
    return true;
  }
  return false;
}
