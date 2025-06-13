/**
 * Main Application Script
 * Handles application initialization, navigation, and common UI functionality
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
});

/**
 * Initialize the application
 */
function initializeApp() {
    // Initialize UI based on authentication status
    updateUIBasedOnAuth();

    // Set up event listeners
    setupNavigationListeners();
    setupAuthListeners();

    // Show initial section
    if (isAuthenticated()) {
        switchSection('home-section');
    } else {
        // If not authenticated, show either home or login based on clicked item
        document.getElementById('get-started-btn')?.addEventListener('click', function() {
            showLoginForm();
        });
    }

}

/**
 * Update UI elements based on authentication status
 */
function updateUIBasedOnAuth() {
    const isLoggedIn = isAuthenticated();

    // Show/hide elements based on auth status
    document.querySelectorAll('.auth-hidden').forEach(el => {
        el.style.display = isLoggedIn ? 'none' : 'flex';
    });

    document.querySelectorAll('.auth-visible').forEach(el => {
        el.style.display = isLoggedIn ? 'flex' : 'none';
    });

    // Update username display if authenticated
    if (isLoggedIn) {
        updateUsernameDisplay();
    }
}

/**
 * Set up navigation event listeners
 */
function setupNavigationListeners() {
    // Navigation menu items
    document.getElementById('login-nav')?.addEventListener('click', showLoginForm);
    document.getElementById('register-nav')?.addEventListener('click', showRegisterForm);
    document.getElementById('logout-nav')?.addEventListener('click', logoutUser);

    // Tab switching in auth section
    document.getElementById('login-tab')?.addEventListener('click', function() {
        switchAuthTab('login');
    });

    document.getElementById('register-tab')?.addEventListener('click', function() {
        switchAuthTab('register');
    });
}

/**
 * Set up authentication form listeners
 */
function setupAuthListeners() {
    // Login form submission
    document.getElementById('login-btn')?.addEventListener('click', function(e) {
        e.preventDefault();
        loginUser();
    });

    // Register form submission
    document.getElementById('register-btn')?.addEventListener('click', function(e) {
        e.preventDefault();
        registerUser();
    });
}

/**
 * Switch between application sections
 * @param {string} sectionId - ID of the section to display
 */
function switchSection(sectionId) {
    // Hide all sections
    document.querySelectorAll('section.section').forEach(section => {
        section.classList.remove('active');
    });

    // Show the requested section
    const targetSection = document.getElementById(sectionId);
    if (targetSection) {
        targetSection.classList.add('active');
    }
}

/**
 * Switch between login and register tabs
 * @param {string} activeTab - Which tab to activate ('login' or 'register')
 */
function switchAuthTab(activeTab) {
    // Update tab buttons
    document.getElementById('login-tab').classList.toggle('active', activeTab === 'login');
    document.getElementById('register-tab').classList.toggle('active', activeTab === 'register');

    // Show the appropriate form
    if (activeTab === 'login') {
        document.getElementById('login-form').classList.remove('hidden');
        document.getElementById('register-form').classList.add('hidden');
    } else {
        document.getElementById('login-form').classList.add('hidden');
        document.getElementById('register-form').classList.remove('hidden');
    }
}

/**
 * Show the login form
 */
function showLoginForm() {
    switchSection('auth-section');
    switchAuthTab('login');
}

/**
 * Show the registration form
 */
function showRegisterForm() {
    switchSection('auth-section');
    switchAuthTab('register');
}

/**
 * Login user with credentials
 */
function loginUser() {
    const username = document.getElementById('login-username').value.trim();
    const password = document.getElementById('login-password').value;
    const errorElement = document.getElementById('login-error');

    if (!username || !password) {
        errorElement.textContent = 'Please enter both username and password';
        return;
    }

    // Clear previous errors
    errorElement.textContent = '';

    // Prepare login data
    const loginData = {
        username: username,
        password: password
    };

    // Send login request
    fetch('/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(loginData)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Login failed');
        }
        return response.json();
    })
    .then(data => {
        // Store the JWT token
        localStorage.setItem('jwtToken', data.token);

        // Update UI and redirect
        updateUIBasedOnAuth();
        showToast('success', 'Login successful');
        switchSection('home-section');
    })
    .catch(error => {
        console.error('Login error:', error);
        errorElement.textContent = 'Invalid username or password';
    });
}

/**
 * Register a new user
 */
function registerUser() {
    const username = document.getElementById('register-username').value.trim();
    const email = document.getElementById('register-email').value.trim();
    const password = document.getElementById('register-password').value;
    const confirmPassword = document.getElementById('register-confirm-password').value;
    const errorElement = document.getElementById('register-error');

    // Validate inputs
    if (!username || !email || !password) {
        errorElement.textContent = 'Please fill in all required fields';
        return;
    }

    if (password !== confirmPassword) {
        errorElement.textContent = 'Passwords do not match';
        return;
    }

    // Clear previous errors
    errorElement.textContent = '';

    // Prepare registration data
    const registerData = {
        username: username,
        email: email,
        password: password
    };

    // Send registration request
    fetch('/api/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(registerData)
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(err => { throw err; });
        }
        return response.json();
    })
    .then(data => {
        showToast('success', 'Registration successful. Please login.');
        switchAuthTab('login');
    })
    .catch(error => {
        console.error('Registration error:', error);
        errorElement.textContent = error.message || 'Registration failed. Please try again.';
    });
}

/**
 * Logout the current user
 */
function logoutUser() {
    // Clear stored token
    localStorage.removeItem('jwtToken');

    // Update UI
    updateUIBasedOnAuth();
    showToast('info', 'You have been logged out');

    // Redirect to home
    switchSection('home-section');
}

/**
 * Goes back to form list
 */
function goBackToFormList() {
    loadMyForms();
}

/**
 * Share a form with another user
 */
function shareForm() {
    const shareEmail = document.getElementById('share-email').value.trim();
    const shareMessage = document.getElementById('share-message');

    if (!shareEmail) {
        shareMessage.textContent = 'Please enter a username or email';
        shareMessage.className = 'share-error';
        return;
    }

    // Prepare share data
    const shareData = {
        recipientEmail: shareEmail
    };

    // Send share request
    fetch(`/api/forms/${currentFormId}/share`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${getAuthToken()}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(shareData)
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to share form');
        return response.json();
    })
    .then(data => {
        shareMessage.textContent = 'Form shared successfully';
        shareMessage.className = 'share-success';
        document.getElementById('share-email').value = '';
    })
    .catch(error => {
        console.error('Error sharing form:', error);
        shareMessage.textContent = 'Failed to share form. User not found or already has access.';
        shareMessage.className = 'share-error';
    });
}

/**
 * Submit form response
 */
function submitFormResponse() {
    const collabFormFields = document.getElementById('collab-form-fields');
    const formData = {};
    let hasErrors = false;

    // Collect field values
    collabFormFields.querySelectorAll('.collab-field').forEach(fieldElement => {
        const fieldId = fieldElement.dataset.fieldId;
        const fieldType = getFieldTypeFromElement(fieldElement);

        let value;

        switch (fieldType) {
            case 'CHECKBOX':
                const checkedValues = [];
                fieldElement.querySelectorAll('input[type="checkbox"]:checked').forEach(checkbox => {
                    checkedValues.push(checkbox.value);
                });
                value = checkedValues;
                break;

            case 'RADIO':
                const checkedRadio = fieldElement.querySelector('input[type="radio"]:checked');
                value = checkedRadio ? checkedRadio.value : null;
                break;

            default:
                const input = fieldElement.querySelector('input, textarea, select');
                value = input ? input.value : null;
                break;
        }

        // Check required fields
        const isRequired = fieldElement.querySelector('.field-required') !== null;
        if (isRequired && (value === null || value === '' || (Array.isArray(value) && value.length === 0))) {
            fieldElement.classList.add('error');
            hasErrors = true;
        } else {
            fieldElement.classList.remove('error');
            formData[fieldId] = value;
        }
    });

    if (hasErrors) {
        showToast('error', 'Please fill in all required fields');
        return;
    }

    // Send form response
    fetch(`/api/forms/${currentFormId}/submit`, {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${getAuthToken()}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ responses: formData })
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to submit form response');
        return response.json();
    })
    .then(data => {
        showToast('success', 'Form submitted successfully');
        loadMyForms();
    })
    .catch(error => {
        console.error('Error submitting form:', error);
        showToast('error', 'Failed to submit form');
    });
}

/**
 * Get field type from field element
 * @param {HTMLElement} fieldElement - The field element
 * @returns {string} Field type
 */
function getFieldTypeFromElement(fieldElement) {
    if (fieldElement.querySelector('textarea')) return 'TEXTAREA';
    if (fieldElement.querySelector('input[type="number"]')) return 'NUMBER';
    if (fieldElement.querySelector('input[type="date"]')) return 'DATE';
    if (fieldElement.querySelector('select')) return 'DROPDOWN';
    if (fieldElement.querySelector('input[type="checkbox"]')) return 'CHECKBOX';
    if (fieldElement.querySelector('input[type="radio"]')) return 'RADIO';
    return 'TEXT';
}

/**
 * Show a toast notification
 * @param {string} type - Type of toast ('success', 'error', 'info')
 * @param {string} message - Message to display
 */
function showToast(type, message) {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toast-message');
    const toastIcon = document.getElementById('toast-icon');

    // Set message and icon based on type
    toastMessage.textContent = message;

    switch (type) {
        case 'success':
            toastIcon.className = 'fas fa-check-circle';
            break;
        case 'error':
            toastIcon.className = 'fas fa-exclamation-circle';
            break;
        case 'info':
        default:
            toastIcon.className = 'fas fa-info-circle';
            break;
    }

    // Show toast
    toast.classList.add('active');

    // Hide after 5 seconds
    setTimeout(() => {
        toast.classList.remove('active');
    }, 5000);
}

/**
 * Debounce function to limit the rate at which a function can fire
 * @param {Function} func - Function to debounce
 * @param {number} wait - Milliseconds to wait
 * @returns {Function} Debounced function
 */
function debounce(func, wait) {
    let timeout;
    return function(...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
}