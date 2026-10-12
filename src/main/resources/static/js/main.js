/**
 * CampusFind — Home Page Search, Filter & Display Logic
 */

let currentFilters = {
    query: '',
    type: 'ALL',
    category: 'ALL',
    location: 'ALL',
    status: 'OPEN',
    page: 1,
    size: 12
};

let debounceTimer = null;

document.addEventListener('DOMContentLoaded', () => {
    loadPlatformStats();
    loadItems();
    setupEventListeners();
});

function setupEventListeners() {
    // Search input debounced
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                currentFilters.query = e.target.value.trim();
                currentFilters.page = 1;
                loadItems();
            }, 300);
        });
    }

    // Type pills
    const pills = document.querySelectorAll('.type-pill');
    pills.forEach(pill => {
        pill.addEventListener('click', () => {
            pills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            currentFilters.type = pill.getAttribute('data-type');
            currentFilters.page = 1;
            loadItems();
        });
    });

    // Category filter
    const catSelect = document.getElementById('category-filter');
    if (catSelect) {
        catSelect.addEventListener('change', (e) => {
            currentFilters.category = e.target.value;
            currentFilters.page = 1;
            loadItems();
        });
    }

    // Location filter
    const locSelect = document.getElementById('location-filter');
    if (locSelect) {
        locSelect.addEventListener('change', (e) => {
            currentFilters.location = e.target.value;
            currentFilters.page = 1;
            loadItems();
        });
    }

    // Status filter
    const statusSelect = document.getElementById('status-filter');
    if (statusSelect) {
        statusSelect.addEventListener('change', (e) => {
            currentFilters.status = e.target.value;
            currentFilters.page = 1;
            loadItems();
        });
    }
}

async function loadPlatformStats() {
    try {
        const res = await api.get('/api/items/stats');
        if (res && res.success && res.data) {
            const data = res.data;
            document.getElementById('stat-total').textContent = data.totalReports || 0;
            document.getElementById('stat-lost').textContent = data.lostReports || 0;
            document.getElementById('stat-found').textContent = data.foundReports || 0;
            document.getElementById('stat-resolved').textContent = data.resolvedReports || 0;
        }
    } catch (err) {
        console.warn('Could not load stats:', err);
    }
}

async function loadItems() {
    const grid = document.getElementById('items-grid');
    const countBadge = document.getElementById('results-count');
    if (!grid) return;

    grid.innerHTML = `
        <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: var(--text-muted);">
            <div style="display:inline-block; width:30px; height:30px; border:3px solid var(--lavender-border); border-top-color:var(--primary); border-radius:50%; animation:spin 0.8s linear infinite;"></div>
            <p style="margin-top:12px;">Searching directory...</p>
        </div>
    `;

    const params = new URLSearchParams({
        query: currentFilters.query,
        type: currentFilters.type,
        category: currentFilters.category,
        location: currentFilters.location,
        status: currentFilters.status,
        page: currentFilters.page,
        size: currentFilters.size
    });

    try {
        const res = await api.get(`/api/items/search?${params.toString()}`);
        if (res && res.success && res.data) {
            const items = res.data.items || [];
            const totalCount = res.data.totalCount || 0;

            if (countBadge) {
                countBadge.textContent = `${totalCount} item${totalCount === 1 ? '' : 's'} listed`;
            }

            if (items.length === 0) {
                grid.innerHTML = `
                    <div style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; background: white; border-radius: var(--radius-xl); border: 1px dashed var(--lavender-border);">
                        <div style="font-size: 3rem; margin-bottom: 12px;">🔍</div>
                        <h3 style="margin-bottom: 8px;">No matching reports found</h3>
                        <p style="color: var(--text-muted); max-width: 400px; margin: 0 auto 20px;">Try adjusting your keywords, campus location, or category filters.</p>
                        <a href="/report-item.html" class="btn btn-primary btn-sm">Report a New Item</a>
                    </div>
                `;
                return;
            }

            grid.innerHTML = items.map(renderItemCard).join('');
        }
    } catch (err) {
        console.error('Failed to load items:', err);
        grid.innerHTML = `
            <div style="grid-column: 1 / -1; text-align: center; padding: 40px; color: #EF4444;">
                Failed to load reports. Please check your connection or database.
            </div>
        `;
    }
}

function renderItemCard(item) {
    const isLost = item.type === 'LOST';
    const isResolved = item.status === 'RESOLVED';

    let badgeClass = isLost ? 'badge-lost' : 'badge-found';
    let badgeText = isLost ? 'Lost Item' : 'Found Item';

    if (isResolved) {
        badgeClass = 'badge-resolved';
        badgeText = 'Reunited';
    }

    const fullImageUrl = (window.CAMPUSFIND_CONFIG && typeof window.CAMPUSFIND_CONFIG.resolveAssetUrl === 'function')
        ? window.CAMPUSFIND_CONFIG.resolveAssetUrl(item.imageUrl)
        : item.imageUrl;

    const imageHtml = fullImageUrl
        ? `<img src="${fullImageUrl}" alt="${escapeHtml(item.title)}" class="item-image" loading="lazy">`
        : `
            <div class="item-image-placeholder">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                    <circle cx="8.5" cy="8.5" r="1.5"></circle>
                    <polyline points="21 15 16 10 5 21"></polyline>
                </svg>
                <span>${escapeHtml(item.category)}</span>
            </div>
        `;

    return `
        <article class="item-card">
            <div class="item-image-wrapper">
                <span class="badge ${badgeClass} item-badge-top">${badgeText}</span>
                <span class="badge-category">${escapeHtml(item.category)}</span>
                ${imageHtml}
            </div>
            <div class="item-card-body">
                <h3 class="item-card-title">${escapeHtml(item.title)}</h3>
                <p class="item-card-desc">${escapeHtml(item.description)}</p>
                <div class="item-meta">
                    <div class="item-meta-row">
                        <span>📍</span>
                        <span>${escapeHtml(item.location)}</span>
                    </div>
                    <div class="item-meta-row">
                        <span>📅</span>
                        <span>Date: ${escapeHtml(item.itemDate)}</span>
                    </div>
                </div>
                <div class="item-card-footer">
                    <button class="btn btn-secondary btn-sm" onclick="openItemModal(${item.id})">
                        View Details
                    </button>
                    ${isResolved ? '<span style="font-size:0.8rem; font-weight:700; color:var(--primary);">Closed</span>' : ''}
                </div>
            </div>
        </article>
    `;
}

async function openItemModal(itemId) {
    const modal = document.getElementById('item-modal');
    const modalBody = document.getElementById('modal-body');
    const modalTitle = document.getElementById('modal-title');
    const modalBadge = document.getElementById('modal-badge');
    if (!modal) return;

    modalBody.innerHTML = '<p style="text-align:center; padding:20px;">Loading item details...</p>';
    modal.classList.add('active');

    try {
        const res = await api.get(`/api/items/${itemId}`);
        if (res && res.success && res.data) {
            const item = res.data;
            const isLost = item.type === 'LOST';
            const isResolved = item.status === 'RESOLVED';

            modalTitle.textContent = item.title;
            modalBadge.className = `badge ${isResolved ? 'badge-resolved' : (isLost ? 'badge-lost' : 'badge-found')}`;
            modalBadge.textContent = isResolved ? 'Resolved' : (isLost ? 'Lost' : 'Found');

            const fullModalImg = (window.CAMPUSFIND_CONFIG && typeof window.CAMPUSFIND_CONFIG.resolveAssetUrl === 'function')
                ? window.CAMPUSFIND_CONFIG.resolveAssetUrl(item.imageUrl)
                : item.imageUrl;

            const imageSection = fullModalImg
                ? `<div style="margin-bottom:20px; border-radius:var(--radius-lg); overflow:hidden; max-height:280px;">
                     <img src="${fullModalImg}" style="width:100%; height:100%; object-fit:cover;">
                   </div>`
                : '';

            modalBody.innerHTML = `
                ${imageSection}
                <div style="margin-bottom:16px;">
                    <h4 style="font-size:0.95rem; color:var(--text-muted); text-transform:uppercase; margin-bottom:4px;">Description</h4>
                    <p style="font-size:1.02rem; color:var(--text-main); white-space:pre-line;">${escapeHtml(item.description)}</p>
                </div>
                
                <div style="display:grid; grid-template-columns:1fr 1fr; gap:16px; margin-bottom:20px; background:var(--lavender-subtle); padding:16px; border-radius:var(--radius-md);">
                    <div>
                        <span style="font-size:0.82rem; color:var(--text-muted); font-weight:600;">CATEGORY</span>
                        <p style="font-weight:700; color:var(--text-main);">${escapeHtml(item.category)}</p>
                    </div>
                    <div>
                        <span style="font-size:0.82rem; color:var(--text-muted); font-weight:600;">DATE</span>
                        <p style="font-weight:700; color:var(--text-main);">${escapeHtml(item.itemDate)}</p>
                    </div>
                    <div>
                        <span style="font-size:0.82rem; color:var(--text-muted); font-weight:600;">CAMPUS LOCATION</span>
                        <p style="font-weight:700; color:var(--text-main);">${escapeHtml(item.location)}</p>
                    </div>
                    <div>
                        <span style="font-size:0.82rem; color:var(--text-muted); font-weight:600;">REPORTED BY</span>
                        <p style="font-weight:700; color:var(--text-main);">${escapeHtml(item.reporterName || 'Student/Staff')}</p>
                    </div>
                </div>

                <div style="background:#FFFFFF; border:1.5px solid var(--lavender-border); border-radius:var(--radius-md); padding:16px;">
                    <h4 style="font-size:0.9rem; color:var(--primary); font-weight:700; margin-bottom:6px;">Contact Information</h4>
                    <p style="font-size:1.05rem; font-weight:700; color:var(--text-main);">${escapeHtml(item.contactInfo)}</p>
                </div>
            `;
        }
    } catch (err) {
        modalBody.innerHTML = '<p style="color:#EF4444; padding:20px;">Could not load item details.</p>';
    }
}

function closeItemModal() {
    const modal = document.getElementById('item-modal');
    if (modal) modal.classList.remove('active');
}
