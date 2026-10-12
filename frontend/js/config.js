/**
 * CampusFind — API Base URL Configuration for Online Deployment
 * 
 * How this works:
 * - If running locally (localhost:8080), it uses '' (same-origin relative paths).
 * - If running on a local frontend server (e.g. port 3000 or 5500), it points to http://localhost:8080.
 * - When deployed on Vercel, it connects to your deployed Render backend URL.
 * 
 * To connect to your Render backend:
 * Update BACKEND_URL below with your deployed Render URL (without trailing slash).
 */
window.CAMPUSFIND_CONFIG = {
    // Replace with your deployed Render backend URL (e.g. 'https://campusfind-api.onrender.com')
    BACKEND_URL: 'https://campusfind-api.onrender.com',

    getApiBaseUrl() {
        // 1. Allow manual runtime override via localStorage for easy testing
        const override = localStorage.getItem('CAMPUSFIND_API_URL');
        if (override) {
            return override.replace(/\/+$/, '');
        }

        // 2. Local development auto-detection
        if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
            if (window.location.port === '8080') {
                return ''; // Monolithic same-origin
            }
            return 'http://localhost:8080'; // Frontend live server accessing local backend
        }

        // 3. Deployed production on Vercel
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
