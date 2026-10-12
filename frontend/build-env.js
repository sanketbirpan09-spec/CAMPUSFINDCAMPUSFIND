/**
 * CampusFind — Build step for Vercel
 * Reads process.env.API_URL or process.env.BACKEND_URL and injects it into js/env.js
 */
const fs = require('fs');
const path = require('path');

const rawApiUrl = process.env.API_URL || process.env.BACKEND_URL || '';
const cleanApiUrl = rawApiUrl ? rawApiUrl.trim().replace(/\/+$/, '') : '';

const content = `// Generated automatically during Vercel build / deployment
window.__ENV__ = {
    API_URL: ${JSON.stringify(cleanApiUrl)}
};
`;

const targetDir = path.join(__dirname, 'js');
if (!fs.existsSync(targetDir)) {
    fs.mkdirSync(targetDir, { recursive: true });
}

fs.writeFileSync(path.join(targetDir, 'env.js'), content, 'utf8');
console.log('✅ Generated js/env.js with API_URL:', cleanApiUrl || '(none specified, using fallback config)');
