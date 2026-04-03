import API_BASE_URL from './config.js';

const getAuthToken = () => localStorage.getItem('token');
const getAccountId = () => localStorage.getItem('accountId');

const signOutAndRedirect = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('accountId');
  window.location.href = '/login';
};

export async function apiFetch(path, options = {}) {
  const token = getAuthToken();
  const accountId = getAccountId();

  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  if (options.includeAccountId !== false && accountId) {
    const separator = path.includes('?') ? '&' : '?';
    path = `${path}${separator}accountId=${encodeURIComponent(accountId)}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    credentials: 'include',
    ...options,
    headers,
  });

  if (response.status === 401) {
    signOutAndRedirect();
    throw new Error('Unauthorized');
  }

  if (response.status === 503) {
    if (window.triggerBootingScreen) {
      window.triggerBootingScreen();
    }
    throw new Error('Service Unavailable');
  }

  // If successful response, reset booting state
  if (response.ok && window.resetBootingScreen) {
    window.resetBootingScreen();
  }

  return response;
}
