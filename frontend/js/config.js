/**
 * CampusFind — API Base URL Configuration for Online Deployment
 * 
 * Order of precedence for API URL:
 * 1. window.__ENV__.API_URL (Set via Vercel Environment Variable "API_URL" during build)
 * 2. localStorage.getItem('CAMPUSFIND_API_URL') (Runtime test override in browser console)
 * 3. Localhost auto-detection (localhost:8080 => relative '', other ports => http://localhost:8080)
 * 4. Fallback BACKEND_URL (e.g. Render production URL)
 */
window.CAMPUSFIND_CONFIG = {
    // Fallback Render backend URL if environment variable is not configured
    BACKEND_URL: 'https://campusfind-api.onrender.com',

    getApiBaseUrl() {
        // 1. Check if set via Vercel build-time environment variable (API_URL or BACKEND_URL)
        if (typeof window !== 'undefined' && window.__ENV__ && window.__ENV__.API_URL && window.__ENV__.API_URL.trim() !== '') {
            return window.__ENV__.API_URL.trim().replace(/\/+$/, '');
        }

        // 2. Allow manual runtime override via localStorage for easy testing/debugging
        if (typeof localStorage !== 'undefined') {
            const override = localStorage.getItem('CAMPUSFIND_API_URL');
            if (override) {
                return override.replace(/\/+$/, '');
            }
        }

        // 3. Local development auto-detection
        if (typeof window !== 'undefined' && (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')) {
            if (window.location.port === '8080') {
                return ''; // Monolithic same-origin
            }
            return 'http://localhost:8080'; // Frontend live server accessing local backend
        }

        // 4. Fallback production backend URL
        return this.BACKEND_URL.replace(/\/+$/, '');
    },

    // Resolves full asset URL (e.g., uploaded item images)
    resolveAssetUrl(url) {
        if (!url) return null;
        if (url.startsWith('http://') || url.startsWith('https://')) {
            return url;
        }
        return `${this.getApiBaseUrl()}${url}`;
    }
};
