const API_BASE = '';

function getToken() {
  return localStorage.getItem('token');
}

function getUser() {
  const user = localStorage.getItem('user');
  return user ? JSON.parse(user) : null;
}

function setSession(token, user) {
  localStorage.setItem('token', token);
  localStorage.setItem('user', JSON.stringify(user));
}

function clearSession() {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
}

function requireLogin() {
  if (!getToken()) {
    window.location.href = 'index.html';
  }
}

function requireAdmin() {
  requireLogin();
  const user = getUser();
  if (!user || user.role !== 'ADMIN') {
    alert('Admin access required');
    window.location.href = 'vehicles.html';
  }
}

function logout() {
  clearSession();
  window.location.href = 'index.html';
}

async function apiFetch(path, options = {}) {
  const headers = options.headers || {};
  headers['Content-Type'] = 'application/json';

  const token = getToken();
  if (token) {
    headers['Authorization'] = 'Bearer ' + token;
  }

  const response = await fetch(API_BASE + path, { ...options, headers });

  if (response.status === 401) {
    clearSession();
    window.location.href = 'index.html';
    return null;
  }

  const contentType = response.headers.get('content-type');
  let data = null;
  if (contentType && contentType.includes('application/json')) {
    data = await response.json();
  }

  if (!response.ok) {
    const errorMsg = data && data.error ? data.error : 'Request failed with status ' + response.status;
    throw new Error(errorMsg);
  }

  return data;
}

function showError(containerId, message) {
  const el = document.getElementById(containerId);
  if (el) {
    el.textContent = message;
    el.classList.remove('hidden');
  }
}

function clearError(containerId) {
  const el = document.getElementById(containerId);
  if (el) {
    el.textContent = '';
    el.classList.add('hidden');
  }
}

