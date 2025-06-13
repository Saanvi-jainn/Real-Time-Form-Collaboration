/**
 * Form Management Module
 * Handles form creation, editing, listing, and collaboration features
 */

// Form state management
let currentFormId = null;
let currentFields = [];
let isEditMode = false;
let stompClient = null;

/**
 * Initialize form functionality
 */
function initFormFunctions() {
    // Navigation elements
    document.getElementById('create-form-nav')?.addEventListener('click', showFormEditor);
    document.getElementById('my-forms-nav')?.addEventListener('click', loadMyForms);
    document.getElementById('shared-forms-nav')?.addEventListener('click', loadSharedForms);

    // Form list elements
    document.getElementById('new-form-btn')?.addEventListener('click', showFormEditor);
    document.getElementById('search-forms')?.addEventListener('input', debounce(filterForms, 300));
    document.getElementById('active-only')?.addEventListener('change', filterForms);

    // Form editor elements
    document.getElementById('save-form-btn')?.addEventListener('click', saveForm);
    document.getElementById('cancel-form-btn')?.addEventListener('click', cancelFormEdit);
    document.getElementById('add-field-btn')?.addEventListener('click', showFieldModal);

    // Field modal elements
    document.getElementById('save-field-btn')?.addEventListener('click', saveField);
    document.getElementById('cancel-field-btn')?.addEventListener('click', hideFieldModal);
    document.getElementById('field-type')?.addEventListener('change', toggleOptionsField);
    document.querySelector('.close-modal')?.addEventListener('click', hideFieldModal);

    // Collaboration form elements
    document.getElementById('back-btn')?.addEventListener('click', goBackToFormList);
    document.getElementById('share-form-btn')?.addEventListener('click', shareForm);
    document.getElementById('submit-form-btn')?.addEventListener('click', submitFormResponse);
}

/**
 * Shows the form editor section for creating a new form
 */
function showFormEditor() {
    switchSection('form-editor-section');
    resetFormEditor();
    document.getElementById('form-editor-title').textContent = 'Create New Form';
    isEditMode = false;
    currentFormId = null;
}

/**
 * Resets the form editor to default state
 */
function resetFormEditor() {
    document.getElementById('form-title').value = '';
    document.getElementById('form-description').value = '';
    document.getElementById('form-fields').innerHTML = '';
    currentFields = [];
}

/**
 * Loads forms created by the current user
 */
function loadMyForms() {
    switchSection('form-list-section');
    document.getElementById('forms-list-title').textContent = 'My Forms';

    const formsContainer = document.getElementById('forms-container');
    formsContainer.innerHTML = '<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i><p>Loading forms...</p></div>';

    fetch('/api/forms/my-forms', {
        headers: {
            'Authorization': `Bearer ${getAuthToken()}`
        }
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to fetch forms');
        return response.json();
    })
    .then(forms => {
        renderFormsList(forms);
    })
    .catch(error => {
        console.error('Error fetching forms:', error);
        formsContainer.innerHTML = `<div class="empty-state"><i class="fas fa-exclamation-circle"></i><p>Error loading forms. Please try again.</p></div>`;
    });
}

/**
 * Loads forms shared with the current user
 */
function loadSharedForms() {
    switchSection('form-list-section');
    document.getElementById('forms-list-title').textContent = 'Forms Shared With Me';

    const formsContainer = document.getElementById('forms-container');
    formsContainer.innerHTML = '<div class="loading-spinner"><i class="fas fa-spinner fa-spin"></i><p>Loading forms...</p></div>';

    fetch('/api/forms/shared-with-me', {
        headers: {
            'Authorization': `Bearer ${getAuthToken()}`
        }
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to fetch forms');
        return response.json();
    })
    .then(forms => {
        renderFormsList(forms);
    })
    .catch(error => {
        console.error('Error fetching forms:', error);
        formsContainer.innerHTML = `<div class="empty-state"><i class="fas fa-exclamation-circle"></i><p>Error loading forms. Please try again.</p></div>`;
    });
}

/**
 * Renders forms in the list section
 * @param {Array} forms - Array of form objects
 */
function renderFormsList(forms) {
    const formsContainer = document.getElementById('forms-container');
    const noFormsMessage = document.getElementById('no-forms-message');

    if (!forms || forms.length === 0) {
        noFormsMessage.classList.remove('hidden');
        formsContainer.innerHTML = '';
        return;
    }

    noFormsMessage.classList.add('hidden');
    formsContainer.innerHTML = '';

    forms.forEach(form => {
        const formCard = document.createElement('div');
        formCard.className = 'form-card';
        formCard.dataset.id = form.id;

        const statusClass = form.active ? 'status-active' : 'status-inactive';
        const statusText = form.active ? 'Active' : 'Inactive';
        const createdDate = new Date(form.createdAt).toLocaleDateString();

        formCard.innerHTML = `
            <h3>${form.title}</h3>
            <p>${form.description || 'No description'}</p>
            <div class="form-card-footer">
                <span class="date">Created: ${createdDate}</span>
                <span class="status ${statusClass}">${statusText}</span>
            </div>
            <div class="form-card-actions">
                <button class="action-btn edit-btn" title="Edit form"><i class="fas fa-edit"></i></button>
                <button class="action-btn delete-btn" title="Delete form"><i class="fas fa-trash-alt"></i></button>
            </div>
        `;

        // Add event listeners to the card and buttons
        formCard.addEventListener('click', (e) => {
            if (!e.target.closest('.action-btn')) {
                openForm(form.id);
            }
        });

        const editBtn = formCard.querySelector('.edit-btn');
        editBtn?.addEventListener('click', (e) => {
            e.stopPropagation();
            editForm(form.id);
        });

        const deleteBtn = formCard.querySelector('.delete-btn');
        deleteBtn?.addEventListener('click', (e) => {
            e.stopPropagation();
            deleteForm(form.id);
        });

        formsContainer.appendChild(formCard);
    });
}

/**
 * Filter forms based on search input and active-only checkbox
 */
function filterForms() {
    const searchInput = document.getElementById('search-forms');
    const activeOnlyCheckbox = document.getElementById('active-only');
    const searchTerm = searchInput.value.toLowerCase();
    const activeOnly = activeOnlyCheckbox.checked;

    // Get all form cards
    const formCards = document.getElementById('forms-container').querySelectorAll('.form-card');
    let visibleCount = 0;

    formCards.forEach(card => {
        const title = card.querySelector('h3').textContent.toLowerCase();
        const description = card.querySelector('p').textContent.toLowerCase();
        const isActive = card.querySelector('.status').textContent === 'Active';

        const matchesSearch = title.includes(searchTerm) || description.includes(searchTerm);
        const matchesActiveFilter = !activeOnly || isActive;

        if (matchesSearch && matchesActiveFilter) {
            card.style.display = '';
            visibleCount++;
        } else {
            card.style.display = 'none';
        }
    });

    // Toggle empty state message
    const noFormsMessage = document.getElementById('no-forms-message');
    if (visibleCount === 0) {
        noFormsMessage.classList.remove('hidden');
    } else {
        noFormsMessage.classList.add('hidden');
    }
}

/**
 * Opens the form editor in edit mode for an existing form
 * @param {number} formId - The ID of the form to edit
 */
function editForm(formId) {
    fetch(`/api/forms/${formId}`, {
        headers: {
            'Authorization': `Bearer ${getAuthToken()}`
        }
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to fetch form');
        return response.json();
    })
    .then(form => {
        // Switch to editor view
        switchSection('form-editor-section');
        document.getElementById('form-editor-title').textContent = 'Edit Form';

        // Populate form fields
        document.getElementById('form-title').value = form.title;
        document.getElementById('form-description').value = form.description || '';

        // Set current form ID and mode
        currentFormId = form.id;
        isEditMode = true;

        // Load fields
        loadFormFields(form.fields || []);
    })
    .catch(error => {
        console.error('Error fetching form:', error);
        showToast('error', 'Failed to load form data');
    });
}

/**
 * Deletes a form
 * @param {number} formId - The ID of the form to delete
 */
function deleteForm(formId) {
    if (confirm('Are you sure you want to delete this form? This action cannot be undone.')) {
        fetch(`/api/forms/${formId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${getAuthToken()}`
            }
        })
        .then(response => {
            if (!response.ok) throw new Error('Failed to delete form');
            showToast('success', 'Form deleted successfully');
            loadMyForms(); // Refresh the list
        })
        .catch(error => {
            console.error('Error deleting form:', error);
            showToast('error', 'Failed to delete form');
        });
    }
}

/**
 * Opens a form in collaboration mode
 * @param {number} formId - Form ID
 */
function openForm(formId) {
    currentFormId = formId;
    switchSection('form-collab-section');

    const formLoading = document.getElementById('form-loading');
    const collabFormTitle = document.getElementById('collab-form-title');
    const collabFormFields = document.getElementById('collab-form-fields');

    formLoading.style.display = 'flex';
    collabFormTitle.textContent = 'Loading Form...';
    collabFormFields.innerHTML = '';

    // Fetch form details
    fetch(`/api/forms/${formId}`, {
        headers: {
            'Authorization': `Bearer ${getAuthToken()}`
        }
    })
    .then(response => {
        if (!response.ok) throw new Error('Failed to fetch form');
        return response.json();
    })
    .then(form => {
        renderCollaborativeForm(form);
    })
    .catch(error => {
        console.error('Error fetching form:', error);
        formLoading.style.display = 'none';
        collabFormTitle.textContent = 'Error Loading Form';
        collabFormFields.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-exclamation-circle"></i>
                <p>Failed to load form. Please try again.</p>
            </div>
        `;
    });
}