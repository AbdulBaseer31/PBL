requireAdmin();
setupNavbar('admin');

async function loadStats() {
  try {
    const stats = await apiFetch('/vehicles/stats');
    if (stats) {
      document.getElementById('stat-total-vehicles').textContent = stats.totalVehicles;
      document.getElementById('stat-active-reservations').textContent = stats.activeReservations;
      document.getElementById('stat-unreserved-cars').textContent = stats.unreservedVehicles;
    }
  } catch (err) {
    showError('admin-error', err.message);
  }
}

async function loadActiveVehicles() {
  const container = document.getElementById('active-vehicles-container');
  try {
    const vehicles = await apiFetch('/vehicles');
    container.innerHTML = '';
    if (!vehicles || vehicles.length === 0) {
      container.innerHTML = '<p class="text-gray-500 italic">No vehicles available.</p>';
      return;
    }
    vehicles.forEach((v) => {
      const item = document.createElement('div');
      item.className = 'flex items-center justify-between bg-gray-100/90 rounded-lg px-3 py-2 border border-gray-200/60';
      item.innerHTML = `
        <span class="font-medium text-gray-700">${v.brand} (${v.numberplate}) - $${v.basePrice}/day</span>
        <button class="text-[#ca5858] hover:text-red-700 flex items-center gap-1 text-[11px] font-medium transition-colors" onclick="removeVehicle('${v.numberplate}')">
          <span>Remove</span>
        </button>
      `;
      container.appendChild(item);
    });
  } catch (err) {
    showError('admin-error', err.message);
  }
}

async function loadAvailableVehiclesDropdown() {
  const select = document.getElementById('select-available-vehicle');
  try {
    const vehicles = await apiFetch('/vehicles?availableOnly=true');
    select.innerHTML = '<option disabled selected value="">Select available vehicle</option>';
    if (vehicles) {
      vehicles.forEach((v) => {
        const opt = document.createElement('option');
        opt.value = v.numberplate;
        opt.textContent = `${v.brand} (${v.numberplate}) - $${v.basePrice}/day`;
        select.appendChild(opt);
      });
    }
  } catch (err) {
    showError('admin-error', err.message);
  }
}

async function removeVehicle(plate) {
  if (!confirm(`Are you sure you want to remove vehicle ${plate}?`)) return;
  clearError('admin-error');
  try {
    await apiFetch(`/vehicles/${encodeURIComponent(plate)}`, { method: 'DELETE' });
    await loadStats();
    await loadActiveVehicles();
    await loadAvailableVehiclesDropdown();
  } catch (err) {
    showError('admin-error', err.message);
  }
}

document.getElementById('form-add-vehicle').addEventListener('submit', async (e) => {
  e.preventDefault();
  clearError('admin-error');
  try {
    const body = {
      numberplate: document.getElementById('input-numberplate').value.trim(),
      brand: document.getElementById('input-brand').value.trim(),
      type: document.getElementById('select-vehicle-type').value,
      basePrice: parseFloat(document.getElementById('input-base-price').value)
    };
    await apiFetch('/vehicles', {
      method: 'POST',
      body: JSON.stringify(body)
    });
    document.getElementById('form-add-vehicle').reset();
    await loadStats();
    await loadActiveVehicles();
    await loadAvailableVehiclesDropdown();
  } catch (err) {
    showError('admin-error', err.message);
  }
});

document.getElementById('form-rent-vehicle').addEventListener('submit', async (e) => {
  e.preventDefault();
  clearError('admin-error');
  try {
    const body = {
      customerId: document.getElementById('input-customer-id').value.trim(),
      numberplate: document.getElementById('select-available-vehicle').value,
      reservDate: document.getElementById('input-reservation-date').value,
      reservPeriod: parseInt(document.getElementById('input-rental-period').value)
    };
    const res = await apiFetch('/reservations', {
      method: 'POST',
      body: JSON.stringify(body)
    });
    alert(`Rental executed successfully! Reservation ID: ${res.reservationId}`);
    document.getElementById('form-rent-vehicle').reset();
    await loadStats();
    await loadAvailableVehiclesDropdown();
  } catch (err) {
    showError('admin-error', err.message);
  }
});

async function checkReservationOrBill() {
  const id = document.getElementById('input-search-reservation').value.trim();
  const resultDiv = document.getElementById('search-result-content');
  if (!id) {
    showError('admin-error', 'Please enter a Reservation ID or Bill ID');
    return;
  }
  clearError('admin-error');
  resultDiv.innerHTML = '<p class="text-gray-500 italic">Searching...</p>';

  try {
    if (id.startsWith('B')) {
      const bill = await apiFetch(`/bills/${id}`);
      resultDiv.innerHTML = `
        <p><span class="font-semibold text-gray-800">Bill ID:</span> #${bill.billId}</p>
        <p><span class="font-semibold text-gray-800">Customer:</span> ${bill.customerName} (ID: ${bill.customerId})</p>
        <p><span class="font-semibold text-gray-800">Vehicle:</span> ${bill.brand} (Plate: ${bill.numberplate})</p>
        <p><span class="font-semibold text-gray-800">Base Price:</span> $${bill.basePrice}/day × ${bill.days} Days</p>
        <p class="pt-1 text-sm font-bold text-gray-900">Total Cost: $${bill.totalCost}</p>
      `;
    } else {
      const res = await apiFetch(`/reservations/${id}`);
      resultDiv.innerHTML = `
        <p><span class="font-semibold text-gray-800">Reservation ID:</span> #${res.reservationId}</p>
        <p><span class="font-semibold text-gray-800">Customer ID:</span> ${res.customerId}</p>
        <p><span class="font-semibold text-gray-800">Vehicle:</span> ${res.numberplate}</p>
        <p><span class="font-semibold text-gray-800">Period:</span> ${res.reservPeriod} days from ${res.reservDate}</p>
      `;
    }
  } catch (err) {
    resultDiv.innerHTML = `<p class="text-red-600">${err.message}</p>`;
  }
}

async function calculateBillForReservation() {
  const id = document.getElementById('input-search-reservation').value.trim();
  const resultDiv = document.getElementById('search-result-content');
  if (!id || !id.startsWith('R')) {
    showError('admin-error', 'Please enter a valid Reservation ID (starting with R) to calculate/generate bill');
    return;
  }
  clearError('admin-error');
  try {
    const bill = await apiFetch('/bills', {
      method: 'POST',
      body: JSON.stringify({ reservationId: id })
    });
    resultDiv.innerHTML = `
      <p><span class="font-semibold text-gray-800">Bill ID:</span> #${bill.billId}</p>
      <p><span class="font-semibold text-gray-800">Customer:</span> ${bill.customerName} (ID: ${bill.customerId})</p>
      <p><span class="font-semibold text-gray-800">Vehicle:</span> ${bill.brand} (Plate: ${bill.numberplate})</p>
      <p><span class="font-semibold text-gray-800">Base Price:</span> $${bill.basePrice}/day × ${bill.days} Days</p>
      <p class="pt-1 text-sm font-bold text-gray-900">Total Cost: $${bill.totalCost}</p>
    `;
  } catch (err) {
    resultDiv.innerHTML = `<p class="text-red-600">${err.message}</p>`;
  }
}

document.getElementById('btn-check-reservations').addEventListener('click', checkReservationOrBill);
document.getElementById('btn-search-reservation').addEventListener('click', checkReservationOrBill);
document.getElementById('btn-calculate-bill').addEventListener('click', calculateBillForReservation);

loadStats();
loadActiveVehicles();
loadAvailableVehiclesDropdown();

