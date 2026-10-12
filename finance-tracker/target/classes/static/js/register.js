// register.js — Registration view logic
// Finance Tracker Frontend

(function () {
  async function loadRegister() {
    // If already authenticated, skip to dashboard.
    if (redirectIfAuthed()) return;

    const form = document.getElementById('register-form');
    const errorEl = document.getElementById('register-error');

    clearMessage(errorEl);

    // Replace form node to clear any stacked listeners.
    const newForm = form.cloneNode(true);
    form.parentNode.replaceChild(newForm, form);

    newForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      clearMessage(document.getElementById('register-error'));

      const email = newForm.querySelector('#register-email').value.trim();
      const password = newForm.querySelector('#register-password').value;
      const displayName = newForm.querySelector('#register-display-name').value.trim();

      // Basic client-side validation.
      if (!email || !email.includes('@')) {
        showError(document.getElementById('register-error'), 'Please enter a valid email address.');
        return;
      }
      if (!password) {
        showError(document.getElementById('register-error'), 'Password is required.');
        return;
      }

      const btn = newForm.querySelector('#register-submit');
      setLoading(btn, 'Creating account…');

      try {
        // Step 1: Register.
        const registerBody = { email, password };
        if (displayName) registerBody.displayName = displayName;

        await apiFetch('/api/v1/auth/register', {
          method: 'POST',
          body: JSON.stringify(registerBody),
        });

        // Step 2: Auto-login with same credentials.
        const loginData = await apiFetch('/api/v1/auth/login', {
          method: 'POST',
          body: JSON.stringify({ email, password }),
        });

        setToken(loginData.token);
        window.location.hash = '#dashboard';
      } catch (err) {
        showError(document.getElementById('register-error'), err.message || 'Registration failed. Please try again.');
      } finally {
        clearLoading(btn);
      }
    });
  }

  Router.register('register', loadRegister);
})();
