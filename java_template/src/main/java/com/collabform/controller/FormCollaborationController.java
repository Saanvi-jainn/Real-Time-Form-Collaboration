package com.collabform.controller;

import com.collabform.dto.response.FieldValueResponse;
import com.collabform.dto.response.FieldValueUpdateRequest;
import com.collabform.dto.websocket.*;
import com.collabform.model.User;
import com.collabform.service.CollaborationService;
import com.collabform.service.FormResponseService;
import com.collabform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for handling form collaboration and real-time updates.
 * Includes both REST endpoints and WebSocket message handlers.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class FormCollaborationController {

    private final FormResponseService formResponseService;
    private final CollaborationService collaborationService;
    private final UserService userService;

    /**
     * Get all field values for a form response.
     *
     * @param formId The ID of the form
     * @return List of field value responses
     */
    @GetMapping("/api/forms/{formId}/values")
    public ResponseEntity<List<FieldValueResponse>> getFormValues(@PathVariable Long formId) {
        log.info("Fetching form values for form ID: {}", formId);
        List<FieldValueResponse> values = formResponseService.getFormResponseValues(formId);
        return ResponseEntity.ok(values);
    }

    /**
     * Update a field value in a form response.
     *
     * @param formId The ID of the form
     * @param request The field value update request
     * @return The updated field value response
     */
    @PutMapping("/api/forms/{formId}/values")
    public ResponseEntity<FieldValueResponse> updateFieldValue(
            @PathVariable Long formId,
            @Valid @RequestBody FieldValueUpdateRequest request) {
        log.info("Updating field value for form ID: {}, field ID: {}", formId, request.getFieldId());
        FieldValueResponse response = formResponseService.updateFieldValue(formId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lock a field for editing.
     *
     * @param formId The ID of the form
     * @param fieldId The ID of the field to lock
     * @param force Whether to force the lock acquisition even if already locked
     * @return Success or failure status
     */
    @PostMapping("/api/forms/{formId}/fields/{fieldId}/lock")
    public ResponseEntity<Map<String, Object>> lockField(
            @PathVariable Long formId,
            @PathVariable Long fieldId,
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        log.info("Locking field ID: {} for form ID: {}, force: {}", fieldId, formId, force);
        boolean success = formResponseService.lockField(formId, fieldId, force);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Lock acquired" : "Could not acquire lock"
        ));
    }

    /**
     * Release a field lock.
     *
     * @param formId The ID of the form
     * @param fieldId The ID of the field to unlock
     * @return Success status
     */
    @DeleteMapping("/api/forms/{formId}/fields/{fieldId}/lock")
    public ResponseEntity<Map<String, Object>> releaseFieldLock(
            @PathVariable Long formId,
            @PathVariable Long fieldId) {
        log.info("Releasing lock for field ID: {} in form ID: {}", fieldId, formId);
        formResponseService.releaseFieldLock(formId, fieldId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Lock released"
        ));
    }

    /**
     * Release all locks held by the current user for a specific form.
     *
     * @param formId The ID of the form
     * @return Success status
     */
    @DeleteMapping("/api/forms/{formId}/locks")
    public ResponseEntity<Map<String, Object>> releaseAllLocks(@PathVariable Long formId) {
        log.info("Releasing all locks for current user in form ID: {}", formId);
        formResponseService.releaseAllUserLocks(formId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "All locks released"
        ));
    }

    /**
     * Submit a form response.
     *
     * @param formId The ID of the form
     * @return Success status
     */
    @PostMapping("/api/forms/{formId}/submit")
    public ResponseEntity<Map<String, Object>> submitFormResponse(@PathVariable Long formId) {
        log.info("Submitting form response for form ID: {}", formId);
        boolean success = formResponseService.submitFormResponse(formId);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Form submitted" : "Form was already submitted"
        ));
    }

    /**
     * Archive a form response.
     *
     * @param formId The ID of the form
     * @return Success status
     */
    @PostMapping("/api/forms/{formId}/archive")
    public ResponseEntity<Map<String, Object>> archiveFormResponse(@PathVariable Long formId) {
        log.info("Archiving form response for form ID: {}", formId);
        boolean success = formResponseService.archiveFormResponse(formId);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "message", success ? "Form archived" : "Form was already archived"
        ));
    }

    /**
     * WebSocket handler for field lock requests.
     * Handles messages sent to /app/form/{formId}/lock
     *
     * @param formId The form ID
     * @param message The lock request message
     */
    @MessageMapping("/form/{formId}/lock")
    public void processLockRequest(
            @DestinationVariable Long formId,
            @Payload LockRequestMessage message) {
        log.debug("Received lock request via WebSocket: formId={}, fieldId={}, userId={}",
                formId, message.getFieldId(), message.getUserId());
        
        User user = userService.getCurrentUser();
        formResponseService.lockField(formId, message.getFieldId(), message.isForce());
    }

    /**
     * WebSocket handler for field updates during typing.
     * This broadcasts temporary field updates to all clients without saving to database.
     * Handles messages sent to /app/form/{formId}/typing
     *
     * @param formId The form ID
     * @param message The field update message
     */
    @MessageMapping("/form/{formId}/typing")
    public void processTypingUpdate(
            @DestinationVariable Long formId,
            @Payload FieldUpdateMessage message) {
        log.debug("Received typing update via WebSocket: formId={}, fieldId={}, userId={}",
                formId, message.getFieldId(), message.getUserId());
        
        User user = userService.getCurrentUser();
        collaborationService.sendTemporaryFieldUpdate(
                formId,
                message.getFieldId(),
                message.getFieldName(),
                message.getFieldType(),
                message.getValue(),
                user
        );
    }

    /**
     * WebSocket handler for user joining a form session.
     * Handles messages sent to /app/form/{formId}/join
     *
     * @param formId The form ID
     * @param message The user join message
     */
    @MessageMapping("/form/{formId}/join")
    public void processUserJoin(
            @DestinationVariable Long formId,
            @Payload UserJoinMessage message) {
        log.debug("User joined form via WebSocket: formId={}, userId={}",
                formId, message.getUserId());
        
        // The join is automatically handled by the WebSocketEventListener
        // when the user subscribes to the form topic
    }

    /**
     * WebSocket handler for user leaving a form session.
     * Handles messages sent to /app/form/{formId}/leave
     *
     * @param formId The form ID
     * @param message The user leave message
     */
    @MessageMapping("/form/{formId}/leave")
    public void processUserLeave(
            @DestinationVariable Long formId,
            @Payload UserLeaveMessage message) {
        log.debug("User explicitly left form via WebSocket: formId={}, userId={}",
                formId, message.getUserId());
        
        User user = userService.getCurrentUser();
        
        // Release all locks held by this user
        formResponseService.releaseAllUserLocks(formId);
        
        // The leave is also automatically handled by the WebSocketEventListener
        // when the user disconnects or unsubscribes
    }
}