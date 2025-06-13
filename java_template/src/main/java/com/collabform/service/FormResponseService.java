package com.collabform.service;

import com.collabform.dto.response.FieldValueResponse;
import com.collabform.dto.response.FieldValueUpdateRequest;
import com.collabform.model.*;
import com.collabform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.collabform.dto.UserDto;

/**
 * Service for form response management operations.
 */
@Service
@RequiredArgsConstructor
public class FormResponseService {

    private final FormRepository formRepository;
    private final FormResponseRepository responseRepository;
    private final FormFieldRepository fieldRepository;
    private final FieldValueRepository fieldValueRepository;
    private final EditLockRepository lockRepository;
    private final UserService userService;
    private final CollaborationService collaborationService;
    private final FormAccessRepository formAccessRepository;

    /**
     * Get all field values for a form response.
     *
     * @param formId The ID of the form
     * @return List of field value responses
     * @throws IllegalArgumentException if the form or response does not exist
     */
    @Transactional(readOnly = true)
    public List<FieldValueResponse> getFormResponseValues(Long formId) {
        User currentUser = userService.getCurrentUser();
        FormResponse response = getResponseAndVerifyAccess(formId, currentUser);
        
        // Get all field values
        List<FieldValue> fieldValues = fieldValueRepository.findByResponseId(response.getId());
        List<FieldValueResponse> valueResponses = fieldValues.stream()
                .map(FieldValueResponse::fromEntity)
                .collect(Collectors.toList());
        
        // Add lock information
        List<EditLock> locks = lockRepository.findByResponseId(response.getId());
        for (EditLock lock : locks) {
            if (!lock.isExpired()) {
                valueResponses.stream()
                        .filter(vr -> vr.getFieldId().equals(lock.getField().getId()))
                        .findFirst()
                        .ifPresent(vr -> {
                            vr.setLocked(true);
                            vr.setLockedBy(UserDto.fromUser(lock.getUser()));
                        });
            }
        }
        
        return valueResponses;
    }

    /**
     * Update a field value in a form response.
     *
     * @param formId The ID of the form
     * @param request The field value update request
     * @return The updated field value response
     * @throws IllegalArgumentException if the form, field, or response does not exist
     * @throws IllegalStateException if the field is locked by another user
     */
    @Transactional
    public FieldValueResponse updateFieldValue(Long formId, FieldValueUpdateRequest request) {
        User currentUser = userService.getCurrentUser();
        FormResponse response = getResponseAndVerifyAccess(formId, currentUser);
        
        // Get the field
        FormField field = fieldRepository.findById(request.getFieldId())
                .orElseThrow(() -> new IllegalArgumentException("Field not found with ID: " + request.getFieldId()));
        
        // Check if the field belongs to the form
        if (!field.getForm().getId().equals(formId)) {
            throw new IllegalArgumentException("Field does not belong to the form");
        }
        
        // Check if the field is locked by another user
        Optional<EditLock> existingLock = lockRepository.findByFieldIdAndResponseId(field.getId(), response.getId());
        if (existingLock.isPresent() && !existingLock.get().isExpired() && !existingLock.get().getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Field is currently being edited by another user: " + existingLock.get().getUser().getUsername());
        }
        
        // Get or create a field value
        FieldValue fieldValue = fieldValueRepository.findByResponseIdAndFieldId(response.getId(), field.getId())
                .orElseGet(() -> FieldValue.builder()
                        .response(response)
                        .field(field)
                        .build());
        
        // Update the value
        fieldValue.setValue(request.getValue(), currentUser);
        FieldValue savedValue = fieldValueRepository.save(fieldValue);
        
        // Release the lock if requested
        if (request.getReleaseLock() != null && request.getReleaseLock()) {
            existingLock.ifPresent(lock -> lockRepository.delete(lock));
            collaborationService.notifyFieldLockReleased(response.getForm(), field, currentUser);
        }
        
        // Notify collaborators of the update
        collaborationService.notifyFieldUpdate(response.getForm(), field, currentUser, request.getValue());
        
        return FieldValueResponse.fromEntity(savedValue);
    }

    /**
     * Lock a field for editing.
     *
     * @param formId The ID of the form
     * @param fieldId The ID of the field to lock
     * @param force Whether to force the lock acquisition even if already locked
     * @return true if the lock was acquired, false otherwise
     * @throws IllegalArgumentException if the form, field, or response does not exist
     */
    @Transactional
    public boolean lockField(Long formId, Long fieldId, boolean force) {
        User currentUser = userService.getCurrentUser();
        FormResponse response = getResponseAndVerifyAccess(formId, currentUser);
        
        // Get the field
        FormField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new IllegalArgumentException("Field not found with ID: " + fieldId));
        
        // Check if the field belongs to the form
        if (!field.getForm().getId().equals(formId)) {
            throw new IllegalArgumentException("Field does not belong to the form");
        }
        
        // Check if the field is already locked
        Optional<EditLock> existingLock = lockRepository.findByFieldIdAndResponseId(fieldId, response.getId());
        if (existingLock.isPresent() && !existingLock.get().isExpired()) {
            // If the current user already has the lock, refresh it
            if (existingLock.get().getUser().getId().equals(currentUser.getId())) {
                existingLock.get().refresh();
                lockRepository.save(existingLock.get());
                return true;
            }
            
            // If forcing the lock, release the existing lock
            if (force) {
                lockRepository.delete(existingLock.get());
                collaborationService.notifyFieldLockReleased(response.getForm(), field, existingLock.get().getUser());
            } else {
                return false; // Cannot acquire lock
            }
        }
        
        // Create a new lock
        LocalDateTime now = LocalDateTime.now();
        EditLock lock = EditLock.builder()
                .field(field)
                .response(response)
                .user(currentUser)
                .lockTime(now)
                .expiresAt(now.plusSeconds(30)) // Lock expires after 30 seconds
                .build();
        
        lockRepository.save(lock);
        
        // Notify collaborators of the lock
        collaborationService.notifyFieldLocked(response.getForm(), field, currentUser, lock.getExpiresAt());
        
        return true;
    }

    /**
     * Release a field lock.
     *
     * @param formId The ID of the form
     * @param fieldId The ID of the field to unlock
     * @throws IllegalArgumentException if the form, field, or response does not exist
     */
    @Transactional
    public void releaseFieldLock(Long formId, Long fieldId) {
        User currentUser = userService.getCurrentUser();
        FormResponse response = getResponseAndVerifyAccess(formId, currentUser);
        
        // Get the field
        FormField field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new IllegalArgumentException("Field not found with ID: " + fieldId));
        
        // Check if the field belongs to the form
        if (!field.getForm().getId().equals(formId)) {
            throw new IllegalArgumentException("Field does not belong to the form");
        }
        
        // Find and release the lock
        Optional<EditLock> lock = lockRepository.findByFieldIdAndResponseId(fieldId, response.getId());
        lock.ifPresent(l -> {
            // Only the lock owner or an admin can release it
            if (l.getUser().getId().equals(currentUser.getId()) || currentUser.isAdmin()) {
                lockRepository.delete(l);
                collaborationService.notifyFieldLockReleased(response.getForm(), field, l.getUser());
            } else {
                throw new IllegalStateException("Cannot release a lock owned by another user");
            }
        });
    }

    /**
     * Release all locks held by a user for a specific form.
     *
     * @param formId The ID of the form
     * @throws IllegalArgumentException if the form or response does not exist
     */
    @Transactional
    public void releaseAllUserLocks(Long formId) {
        User currentUser = userService.getCurrentUser();
        FormResponse response = getResponseAndVerifyAccess(formId, currentUser);
        
        // Release all locks held by the current user
        lockRepository.deleteByUserIdAndResponseId(currentUser.getId(), response.getId());
        
        // Notify collaborators
        collaborationService.notifyUserLeft(response.getForm(), currentUser);
    }

    /**
     * Submit a form response, changing its status from DRAFT to SUBMITTED.
     *
     * @param formId The ID of the form
     * @return true if the status was changed, false if already submitted
     * @throws IllegalArgumentException if the form or response does not exist
     */
    @Transactional
    public boolean submitFormResponse(Long formId) {
        User currentUser = userService.getCurrentUser();
        FormResponse response = getResponseAndVerifyAccess(formId, currentUser);
        
        // If already submitted, return false
        if (response.getStatus() == ResponseStatus.SUBMITTED) {
            return false;
        }
        
        // Change status to SUBMITTED
        response.setStatus(ResponseStatus.SUBMITTED);
        responseRepository.save(response);
        
        // Notify collaborators of the submission
        collaborationService.notifyFormSubmitted(response.getForm(), currentUser);
        
        return true;
    }

    /**
     * Archive a form response.
     *
     * @param formId The ID of the form
     * @return true if the status was changed, false if already archived
     * @throws IllegalArgumentException if the form or response does not exist
     */
    @Transactional
    public boolean archiveFormResponse(Long formId) {
        User currentUser = userService.getCurrentUser();
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new IllegalArgumentException("Form not found with ID: " + formId));
        
        // Verify ownership (only form owner can archive)
        if (!form.getAdmin().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("Only the form owner can archive responses");
        }
        
        FormResponse response = responseRepository.findByFormId(formId)
                .orElseThrow(() -> new IllegalArgumentException("Response not found for form ID: " + formId));
        
        // If already archived, return false
        if (response.getStatus() == ResponseStatus.ARCHIVED) {
            return false;
        }
        
        // Change status to ARCHIVED
        response.setStatus(ResponseStatus.ARCHIVED);
        responseRepository.save(response);
        
        // Notify collaborators of the archiving
        collaborationService.notifyFormArchived(form, currentUser);
        
        return true;
    }

    /**
     * Clean up expired locks.
     * This method should be called periodically by a scheduled task.
     *
     * @return The number of locks removed
     */
    @Transactional
    public int cleanupExpiredLocks() {
        List<EditLock> expiredLocks = lockRepository.findByExpiresAtBefore(LocalDateTime.now());
        int count = expiredLocks.size();
        
        if (!expiredLocks.isEmpty()) {
            // Notify collaborators of each released lock
            for (EditLock lock : expiredLocks) {
                collaborationService.notifyFieldLockReleased(
                        lock.getResponse().getForm(),
                        lock.getField(),
                        lock.getUser()
                );
            }
            
            // Delete the expired locks
            lockRepository.deleteAll(expiredLocks);
        }
        
        return count;
    }

    /**
     * Get a form response and verify that the user has access to it.
     *
     * @param formId Form ID
     * @param user User requesting access
     * @return The form response
     * @throws IllegalArgumentException if the form or response does not exist or the user does not have access
     */
    private FormResponse getResponseAndVerifyAccess(Long formId, User user) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new IllegalArgumentException("Form not found with ID: " + formId));
        
        // Verify access
        if (!form.getAdmin().getId().equals(user.getId()) && 
                !formAccessRepository.existsByFormIdAndUserId(formId, user.getId())) {
            throw new IllegalArgumentException("You do not have access to this form");
        }
        
        return responseRepository.findByFormId(formId)
                .orElseThrow(() -> new IllegalArgumentException("Response not found for form ID: " + formId));
    }
}