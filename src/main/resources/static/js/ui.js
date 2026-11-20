// Shared UI initialization and navigation setup
function setupNavbar(activeTab) {
  const user = getUser();
  const userNameEl = document.getElementById('nav-user-name');
  if (userNameEl && user) {
    userNameEl.textContent = user.name + (user.role === 'ADMIN' ? ' (Admin)' : '');
  }

  const adminLink = document.getElementById('nav-admin-link');
  if (adminLink && user && user.role === 'ADMIN') {
    adminLink.classList.remove('hidden');
  }
}

