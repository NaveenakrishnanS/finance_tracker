// login.js — Login view logic
// Finance Tracker Frontend

(function () {
  async function loadLogin() {
    // If already authenticated, skip to dashboard.
    if (redirectIfAuthed()) return;

    const form = document.getElementById('login-form');
    const emailInput = document.getElementById('login-email');
    const passwordInput = document.getElementById('login-password');
    const submitBtn = document.getElementById('login-submit');
    const errorEl = document.getElementById('login-error');

    // Clear previous messages each time the view loads.
    clearMessage(errorEl);

    // Remove any previous listener to avoid stacking handlers on hash re-visits.
    const newForm = form.cloneNode(true);
    form.parentNode.replaceChild(newForm, form);

    newForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearMessage(document.getElementById('login-error'));

      const email = newForm.querySelector('#login-email').value.trim();
      const password = newForm.querySelector('#login-password').value;

      // Basic client-side validation.
      if (!email || !email.includes('@')) {
        showError(document.getElementById('login-error'), 'Please enter a valid email address.');
        return;
      }
      if (!password) {
        showError(document.getElementById('login-error'), 'Password is required.');
        return;
      }

      const btn = newForm.querySelector('#login-submit');
      setLoading(btn, 'Signing in…');

      try {
        const data = await apiFetch('/api/v1/auth/login', {
          method: 'POST',
          body: JSON.stringify({ email, password }),
        });
        setToken(data.token);
        window.location.hash = '#dashboard';
      } catch (err) {
        showError(document.getElementById('login-error'), err.message || 'Login failed. Please check your credentials.');
      } finally {
        clearLoading(btn);
      }
    });
  }

  Router.register('login', loadLogin);
})();
