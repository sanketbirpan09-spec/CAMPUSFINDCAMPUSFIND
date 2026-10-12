/**
 * CampusFind — API Client & Shared Utilities (Cross-Origin & Cloud Ready)
 */

const api = {
    // Read cookie by name (used for CSRF token retrieval in same-origin)
    getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    },

    getApiBaseUrl() {
        if (window.CAMPUSFIND_CONFIG && typeof window.CAMPUSFIND_CONFIG.getApiBaseUrl === 'function') {
            return window.CAMPUSFIND_CONFIG.getApiBaseUrl();
        }
        return '';
    },

    resolveUrl(endpoint) {
        if (endpoint.startsWith('http://') || endpoint.startsWith('https://')) {
            return endpoint;
        }
        const base = this.getApiBaseUrl();
        return `${base}${endpoint}`;
    },

    async request(endpoint, options = {}) {
        options.headers = options.headers || {};
        options.credentials = 'include'; // Required for cross-origin sessions and cookies

        // Attach cross-origin authentication token if present
        const authToken = localStorage.getItem('campusfind_auth_token');
        if (authToken) {
            options.headers['X-Auth-Token'] = authToken;
            options.headers['Authorization'] = `Bearer ${authToken}`;
        }

        const fullUrl = this.resolveUrl(endpoint);

        try {
            const response = await fetch(fullUrl, options);
            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const message = (data && data.message) ? data.message : `Request failed with status ${response.status}`;
                const error = new Error(message);
                error.status = response.status;
                error.data = data;
                throw error;
            }

            return data;
        } catch (err) {
            console.error('API Error:', err);
            throw err;
        }
    },

    async get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    async post(endpoint, body) {
        const res = await this.request(endpoint, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        // If login or register returns an auth token, store it locally for cross-origin requests
        if (res && res.success && res.data && res.data.token) {
            localStorage.setItem('campusfind_auth_token', res.data.token);
            localStorage.setItem('campusfind_user', JSON.stringify(res.data));
        }

        return res;
    },

    async postForm(endpoint, formData) {
        return this.request(endpoint, {
            method: 'POST',
            body: formData
        });
    },

    async put(endpoint, body = {}) {
        return this.request(endpoint, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
    },

    async delete(endpoint, body = {}) {
        return this.request(endpoint, {
            method: 'DELETE',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
    },

    // Session / Token user profile helper
    async getCurrentUser() {
        try {
            const res = await this.get('/api/auth/me');
            if (res && res.success && res.data) {
                localStorage.setItem('campusfind_user', JSON.stringify(res.data));
                return res.data;
            }
            // Clear local token if server says unauthenticated
            localStorage.removeItem('campusfind_auth_token');
            localStorage.removeItem('campusfind_user');
            return null;
        } catch {
            return null;
        }
    },

    async logout() {
        try {
            await this.post('/api/auth/logout', {});
        } catch (err) {
            console.warn('Logout API notification failed, clearing local session:', err);
        } finally {
            localStorage.removeItem('campusfind_auth_token');
            localStorage.removeItem('campusfind_user');
            this.showToast('Logged out successfully', 'info');
            setTimeout(() => {
                window.location.href = '/index.html';
            }, 400);
        }
    },

    // Dynamic Navigation Header updater
    async updateNavAuthUI() {
        const user = await this.getCurrentUser();
        const navAuthContainer = document.getElementById('nav-auth-actions');
        if (!navAuthContainer) return;

        if (user) {
            const isAdmin = user.role === 'ROLE_ADMIN';
            navAuthContainer.innerHTML = `
                <div style="display:flex; align-items:center; gap:12px; flex-wrap:wrap;">
                    <a href="/report-item.html" class="btn btn-primary btn-sm">
                        <span>+</span> Report Item
                    </a>
                    <a href="/my-reports.html" class="btn btn-secondary btn-sm">My Reports</a>
                    ${isAdmin ? '<a href="/admin.html" class="btn btn-outline btn-sm" style="border-color:#7C3AED; color:#7C3AED; font-weight:700;">Admin Panel</a>' : ''}
                    <div style="display:flex; align-items:center; gap:8px; margin-left:6px;">
                        <span style="font-size:0.88rem; font-weight:700; color:var(--text-main);">${escapeHtml(user.fullName)}</span>
                        <button onclick="api.logout()" class="btn btn-outline btn-sm" title="Log Out">Sign Out</button>
                    </div>
                </div>
            `;
        } else {
            navAuthContainer.innerHTML = `
                <a href="/login.html" class="btn btn-outline btn-sm">Log In</a>
                <a href="/register.html" class="btn btn-primary btn-sm">Sign Up</a>
            `;
        }
    },

    // Toast notification
    showToast(message, type = 'info') {
        let container = document.getElementById('toast-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toast-container';
            container.className = 'toast-container';
            document.body.appendChild(container);
        }

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.innerHTML = `
            <span>${escapeHtml(message)}</span>
        `;
        container.appendChild(toast);

        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(10px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }
};

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// Auto-run nav auth UI check when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    api.updateNavAuthUI();
});
