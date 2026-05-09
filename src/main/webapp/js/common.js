window.SIS = (function () {
    async function parseResponse(response) {
        const contentType = response.headers.get('content-type') || '';
        const isJson = contentType.includes('application/json');
        const data = isJson ? await response.json() : await response.text();
        if (!response.ok) {
            const message = typeof data === 'string' ? data : (data.message || 'Request failed.');
            throw new Error(message);
        }
        return data;
    }

    async function apiGet(url) {
        const response = await fetch(url, { credentials: 'same-origin' });
        return parseResponse(response);
    }

    async function apiPost(url, payload, useFormData = false) {
        const options = {
            method: 'POST',
            credentials: 'same-origin'
        };

        if (useFormData) {
            options.body = payload;
        } else {
            options.headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
            options.body = new URLSearchParams(payload);
        }

        const response = await fetch(url, options);
        return parseResponse(response);
    }

    function showMessage(target, message, type = 'info') {
        if (!target) return;
        target.textContent = message;
        target.classList.remove('hidden', 'success', 'error', 'info');
        target.classList.add(type);
    }

    function clearMessage(target) {
        if (!target) return;
        target.textContent = '';
        target.classList.add('hidden');
        target.classList.remove('success', 'error', 'info');
    }

    function fillProfile(session) {
        document.querySelectorAll('[data-full-name]').forEach(node => {
            node.textContent = session.fullName || session.name || 'User';
        });

        document.querySelectorAll('[data-role-label]').forEach(node => {
            const role = String(session.role || 'USER');
            node.textContent = role.charAt(0) + role.slice(1).toLowerCase();
        });

        document.querySelectorAll('[data-avatar]').forEach(node => {
            const parts = String(session.fullName || session.name || 'User').trim().split(/\s+/);
            const initials = (parts[0]?.[0] || '') + (parts[1]?.[0] || '');
            node.textContent = initials || 'US';
        });
    }

    async function requireSession(expectedRole) {
        const session = await apiGet('api/session');

        if (!session || !session.userId || !session.role) {
            window.location.href = 'index.html';
            throw new Error('Login required.');
        }

        if (expectedRole && String(session.role).toUpperCase() !== String(expectedRole).toUpperCase()) {
            window.location.href = 'index.html';
            throw new Error('Access denied.');
        }

        fillProfile(session);
        return session;
    }

    function escapeHtml(value) {
        return String(value ?? '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    return {
        apiGet,
        apiPost,
        clearMessage,
        showMessage,
        requireSession,
        escapeHtml
    };
})();
