// JWT Token Storage Key
const TOKEN_KEY = 'jwtToken';

/**
 * Check if user is authenticated
 * @returns {boolean} True if user has a valid token
 */
function isAuthenticated() {
    return localStorage.getItem(TOKEN_KEY) !== null;
}

/**
 * Get the stored JWT token
 * @returns {string|null} The JWT token or null if not authenticated
 */
function getAuthToken() {
    return localStorage.getItem(TOKEN_KEY);
}

/**
 * Get authorization header with Bearer token
 * @returns {string|null} The Authorization header value or null
 */
function getAuthHeader() {
    const token = getAuthToken();
    return token ? `Bearer ${token}` : null;
}

/**
 * Parse JWT token to get user info
 * @param {string} token - JWT token
 * @returns {Object} Decoded token payload
 */
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(atob(base64));
    } catch (e) {
        console.error('Error parsing JWT token', e);
        return {};
    }
}

/**
 * Get current user info from token
 * @returns {Object|null} User info or null if not authenticated
 */
function getCurrentUser() {
    const token = getAuthToken();
    if (!token) return null;

    const tokenData = parseJwt(token);
    // Check if token is expired
    const currentTime = Date.now() / 1000;
    if (tokenData.exp && tokenData.exp < currentTime) {
        // Token expired, logout
        logoutUser();
        return null;
    }

    return tokenData;
}

/**
 * Update username display in the UI
 */
function updateUsernameDisplay() {
    const user = getCurrentUser();
    const usernameDisplay = document.getElementById('username-display');

    if (user && usernameDisplay) {
        usernameDisplay.textContent = user.sub || 'User';
    }
}