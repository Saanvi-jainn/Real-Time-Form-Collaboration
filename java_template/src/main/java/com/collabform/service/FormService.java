package com.collabform.service;

import com.collabform.dto.form.*;
import com.collabform.model.Form;
import com.collabform.model.FormAccess;
import com.collabform.model.FormField;
import com.collabform.model.ResponseStatus;
import com.collabform.model.User;
import com.collabform.repository.FormAccessRepository;
import com.collabform.repository.FormFieldRepository;
import com.collabform.repository.FormRepository;
import com.collabform.repository.FormResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FormService {

    private final FormRepository formRepository;
    private final FormFieldRepository formFieldRepository;
    private final FormResponseRepository formResponseRepository;
    private final FormAccessRepository formAccessRepository;
    private final UserService userService;
    private final CollaborationService collaborationService;

    @Transactional
    public FormResponse createForm(FormCreateRequest request) {
        User currentUser = userService.getCurrentUser();

        Form form = Form.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .admin(currentUser)
                .active(true)
                .fields(new ArrayList<>())
                .accessEntries(new ArrayList<>())
                .build();

        Form savedForm = formRepository.save(form);

        if (request.getFields() != null && !request.getFields().isEmpty()) {
            for (int i = 0; i < request.getFields().size(); i++) {
                FormFieldRequest fieldRequest = request.getFields().get(i);
                FormField field = FormField.builder()
                        .form(savedForm)
                        .fieldName(fieldRequest.getFieldName())
                        .fieldType(fieldRequest.getFieldType())
                        .fieldOptions(fieldRequest.getFieldOptions())
                        .required(fieldRequest.isRequired())
                        .displayOrder(fieldRequest.getDisplayOrder() != null ? fieldRequest.getDisplayOrder() : i)
                        .build();

                formFieldRepository.save(field);
            }
        }

        // ✅ Fully qualified to avoid ambiguity
        com.collabform.model.FormResponse response = com.collabform.model.FormResponse.builder()
                .form(savedForm)
                .status(ResponseStatus.DRAFT)
                .build();

        formResponseRepository.save(response);

        Form reloadedForm = formRepository.findById(savedForm.getId()).orElseThrow();

        return FormResponse.fromEntity(reloadedForm, true, true, true);
    }

    @Transactional(readOnly = true)
    public FormResponse getFormById(Long formId, boolean includeFields, boolean includeResponse, boolean includeCollaborators) {
        User currentUser = userService.getCurrentUser();
        Form form = getFormAndVerifyAccess(formId, currentUser);

        return FormResponse.fromEntity(form, includeFields, includeResponse, includeCollaborators);
    }

    @Transactional
    public FormResponse updateForm(Long formId, FormUpdateRequest request) {
        User currentUser = userService.getCurrentUser();
        Form form = getFormAndVerifyOwnership(formId, currentUser);

        if (request.getTitle() != null) {
            form.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            form.setDescription(request.getDescription());
        }

        if (request.getActive() != null) {
            form.setActive(request.getActive());
        }

        if (request.getFields() != null) {
            if (request.isReplaceAllFields()) {
                formFieldRepository.deleteByFormId(formId);
                form.getFields().clear();

                for (int i = 0; i < request.getFields().size(); i++) {
                    FormFieldRequest fieldRequest = request.getFields().get(i);
                    FormField field = FormField.builder()
                            .form(form)
                            .fieldName(fieldRequest.getFieldName())
                            .fieldType(fieldRequest.getFieldType())
                            .fieldOptions(fieldRequest.getFieldOptions())
                            .required(fieldRequest.isRequired())
                            .displayOrder(fieldRequest.getDisplayOrder() != null ? fieldRequest.getDisplayOrder() : i)
                            .build();

                    formFieldRepository.save(field);
                    form.addField(field);
                }
            } else {
                Map<Long, FormField> existingFieldsMap = form.getFields().stream()
                        .collect(Collectors.toMap(FormField::getId, field -> field));

                for (int i = 0; i < request.getFields().size(); i++) {
                    FormFieldRequest fieldRequest = request.getFields().get(i);

                    if (fieldRequest.getId() != null && existingFieldsMap.containsKey(fieldRequest.getId())) {
                        FormField existingField = existingFieldsMap.get(fieldRequest.getId());
                        existingField.setFieldName(fieldRequest.getFieldName());
                        existingField.setFieldType(fieldRequest.getFieldType());
                        existingField.setFieldOptions(fieldRequest.getFieldOptions());
                        existingField.setRequired(fieldRequest.isRequired());
                        existingField.setDisplayOrder(fieldRequest.getDisplayOrder() != null ? fieldRequest.getDisplayOrder() : i);
                        formFieldRepository.save(existingField);
                    } else {
                        FormField field = FormField.builder()
                                .form(form)
                                .fieldName(fieldRequest.getFieldName())
                                .fieldType(fieldRequest.getFieldType())
                                .fieldOptions(fieldRequest.getFieldOptions())
                                .required(fieldRequest.isRequired())
                                .displayOrder(fieldRequest.getDisplayOrder() != null ? fieldRequest.getDisplayOrder() : i)
                                .build();

                        formFieldRepository.save(field);
                        form.addField(field);
                    }
                }
            }
        }

        form = formRepository.save(form);
        collaborationService.notifyFormUpdate(form);

        return FormResponse.fromEntity(form, true, true, true);
    }

    @Transactional
    public void deleteForm(Long formId) {
        User currentUser = userService.getCurrentUser();
        Form form = getFormAndVerifyOwnership(formId, currentUser);

        collaborationService.notifyFormDeletion(form);
        formRepository.delete(form);
    }

    @Transactional(readOnly = true)
    public List<FormResponse> getMyForms(boolean activeOnly) {
        User currentUser = userService.getCurrentUser();
        List<Form> forms = activeOnly ?
                formRepository.findByAdminAndActive(currentUser, true) :
                formRepository.findByAdmin(currentUser);

        return forms.stream()
                .map(form -> FormResponse.fromEntity(form, false, false, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FormResponse> getSharedForms() {
        User currentUser = userService.getCurrentUser();
        List<Form> forms = formRepository.findFormsAccessibleToUser(currentUser.getId());

        return forms.stream()
                .filter(Form::isActive)
                .map(form -> FormResponse.fromEntity(form, false, false, false))
                .collect(Collectors.toList());
    }

    @Transactional
    public FormShareResponse shareForm(Long formId, FormShareRequest request) {
        User currentUser = userService.getCurrentUser();
        Form form = getFormAndVerifyOwnership(formId, currentUser);

        User targetUser = userService.findByUsernameOrEmail(request.getUsernameOrEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUsernameOrEmail()));

        if (formAccessRepository.existsByFormIdAndUserId(formId, targetUser.getId())) {
            throw new IllegalArgumentException("Form already shared with this user");
        }

        String accessCode = form.generateShareCode();

        FormAccess formAccess = FormAccess.builder()
                .form(form)
                .user(targetUser)
                .accessCode(accessCode)
                .accessGranted(LocalDateTime.now())
                .build();

        formAccessRepository.save(formAccess);
        collaborationService.notifyUserAdded(form, targetUser);

        return FormShareResponse.builder()
                .formId(form.getId())
                .accessCode(accessCode)
                .sharedWithUsername(targetUser.getUsername())
                .sharedWithUserId(targetUser.getId())
                .message(request.getCustomMessage())
                .success(true)
                .build();
    }

    @Transactional(readOnly = true)
    public boolean hasAccessToForm(Long formId, User user) {
        if (user.isAdmin()) {
            return true;
        }

        Optional<Form> formOpt = formRepository.findById(formId);
        if (formOpt.isPresent() && formOpt.get().getAdmin().getId().equals(user.getId())) {
            return true;
        }

        return formAccessRepository.existsByFormIdAndUserId(formId, user.getId());
    }

    private Form getFormAndVerifyAccess(Long formId, User user) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new IllegalArgumentException("Form not found with ID: " + formId));

        if (!hasAccessToForm(formId, user)) {
            throw new IllegalArgumentException("You do not have access to this form");
        }

        return form;
    }

    private Form getFormAndVerifyOwnership(Long formId, User user) {
        Form form = formRepository.findById(formId)
                .orElseThrow(() -> new IllegalArgumentException("Form not found with ID: " + formId));

        if (!form.getAdmin().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You do not own this form");
        }

        return form;
    }
}
