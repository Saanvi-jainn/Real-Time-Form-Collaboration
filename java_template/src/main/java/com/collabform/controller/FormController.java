package com.collabform.controller;

import com.collabform.dto.form.*;
import com.collabform.dto.collaboration.JoinFormRequest;
import com.collabform.service.FormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling form management endpoints.
 */
@RestController
@RequestMapping("/api/forms")
@RequiredArgsConstructor
@Slf4j
public class FormController {

    private final FormService formService;

    /**
     * Create a new form.
     *
     * @param request Form creation details
     * @return The created form response
     */
    @PostMapping
    public ResponseEntity<FormResponse> createForm(@Valid @RequestBody FormCreateRequest request) {
        log.info("Creating new form: {}", request.getTitle());
        FormResponse response = formService.createForm(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a form by ID.
     *
     * @param formId The form ID
     * @return The form response
     */
    @GetMapping("/{formId}")
    public ResponseEntity<FormResponse> getForm(@PathVariable Long formId) {
        log.info("Fetching form with ID: {}", formId);
        FormResponse response = formService.getFormById(formId, true, true, true);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a form.
     *
     * @param formId The form ID
     * @param request Form update details
     * @return The updated form response
     */
    @PutMapping("/{formId}")
    public ResponseEntity<FormResponse> updateForm(
            @PathVariable Long formId,
            @Valid @RequestBody FormUpdateRequest request) {
        log.info("Updating form with ID: {}", formId);
        FormResponse response = formService.updateForm(formId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a form.
     *
     * @param formId The form ID
     * @return No content response
     */
    @DeleteMapping("/{formId}")
    public ResponseEntity<Void> deleteForm(@PathVariable Long formId) {
        log.info("Deleting form with ID: {}", formId);
        formService.deleteForm(formId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all forms created by the current user.
     *
     * @param activeOnly Whether to include only active forms
     * @return List of form responses
     */
    @GetMapping("/my-forms")
    public ResponseEntity<List<FormResponse>> getMyForms(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        log.info("Fetching current user's forms, activeOnly={}", activeOnly);
        List<FormResponse> forms = formService.getMyForms(activeOnly);
        return ResponseEntity.ok(forms);
    }

    /**
     * Get forms shared with the current user.
     *
     * @return List of form responses
     */
    @GetMapping("/shared-with-me")
    public ResponseEntity<List<FormResponse>> getSharedForms() {
        log.info("Fetching forms shared with current user");
        List<FormResponse> forms = formService.getSharedForms();
        return ResponseEntity.ok(forms);
    }

    /**
     * Share a form with another user.
     *
     * @param formId The form ID
     * @param request Form sharing details
     * @return The form share response
     */
    @PostMapping("/{formId}/share")
    public ResponseEntity<FormShareResponse> shareForm(
            @PathVariable Long formId,
            @Valid @RequestBody FormShareRequest request) {
        log.info("Sharing form ID {} with user: {}", formId, request.getUsernameOrEmail());
        FormShareResponse response = formService.shareForm(formId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Join a shared form using an access code.
     *
     * @param request Form join request with access code
     * @return The form response
     */
    @PostMapping("/join")
    public ResponseEntity<FormResponse> joinForm(@Valid @RequestBody JoinFormRequest request) {
        // Note: This endpoint would need to be implemented in the service layer
        log.info("Attempting to join form with access code");
        // FormResponse response = formService.joinForm(request);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}