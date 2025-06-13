package com.collabform.service;

import com.collabform.dto.UserDto;
import com.collabform.dto.websocket.*;
import com.collabform.model.Form;
import com.collabform.model.FormField;
import com.collabform.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Service for handling real-time collaboration functionality and WebSocket messaging.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborationService {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String FORM_TOPIC_PREFIX = "/topic/form/";

    public void notifyFieldUpdate(Form form, FormField field, User user, String value) {
        FieldUpdateMessage message = FieldUpdateMessage.builder()
                .type("FIELD_UPDATE")
                .formId(form.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .fieldId(field.getId())
                .fieldName(field.getFieldName())
                .fieldType(field.getFieldType().toString())
                .value(value)
                .isTemporary(false)
                .timestamp(System.currentTimeMillis())
                .build();

        sendToFormTopic(form.getId(), message);

        log.debug("Notified collaborators of field update: formId={}, fieldId={}, userId={}",
                form.getId(), field.getId(), user.getId());
    }

    public void notifyFieldLocked(Form form, FormField field, User user, LocalDateTime expiresAt) {
        LockAcquiredMessage message = LockAcquiredMessage.create(
                form.getId(),
                user.getId(),
                user.getUsername(),
                field.getId(),
                field.getFieldName(),
                expiresAt.toInstant(ZoneOffset.UTC).toEpochMilli()
        );

        sendToFormTopic(form.getId(), message);

        log.debug("Notified collaborators of field lock: formId={}, fieldId={}, userId={}",
                form.getId(), field.getId(), user.getId());
    }

    public void notifyFieldLockReleased(Form form, FormField field, User user) {
        LockReleasedMessage message = LockReleasedMessage.create(
                form.getId(),
                user.getId(),
                user.getUsername(),
                field.getId(),
                field.getFieldName()
        );

        sendToFormTopic(form.getId(), message);

        log.debug("Notified collaborators of field lock release: formId={}, fieldId={}, userId={}",
                form.getId(), field.getId(), user.getId());
    }

    public void notifyUserJoined(Form form, User user) {
        UserJoinMessage message = UserJoinMessage.create(
                form.getId(),
                user.getId(),
                user.getUsername(),
                user.getRole().toString()
        );

        sendToFormTopic(form.getId(), message);

        log.debug("Notified collaborators of user join: formId={}, userId={}",
                form.getId(), user.getId());
    }

    public void notifyUserLeft(Form form, User user) {
        UserLeaveMessage message = UserLeaveMessage.create(
                form.getId(),
                user.getId(),
                user.getUsername()
        );

        sendToFormTopic(form.getId(), message);

        log.debug("Notified collaborators of user leave: formId={}, userId={}",
                form.getId(), user.getId());
    }

    public void notifyUserAdded(Form form, User newUser) {
        GenericMessage message = GenericMessage.builder()
                .type("USER_ADDED")
                .formId(form.getId())
                .userId(newUser.getId())
                .username(newUser.getUsername())
                .timestamp(System.currentTimeMillis())
                .build();

        sendToFormTopic(form.getId(), message);

        log.debug("Notified collaborators of user addition: formId={}, userId={}",
                form.getId(), newUser.getId());
    }

    public void notifyFormUpdate(Form form) {
        GenericMessage message = GenericMessage.builder()
                .type("FORM_UPDATED")
                .formId(form.getId())
                .userId(form.getAdmin().getId())
                .username(form.getAdmin().getUsername())
                .timestamp(System.currentTimeMillis())
                .build();

        sendToFormTopic(form.getId(), message);

        log.debug("Notified collaborators of form update: formId={}", form.getId());
    }

    public void notifyFormSubmitted(Form form, User submitter) {
        GenericMessage message = GenericMessage.builder()
                .type("FORM_SUBMITTED")
                .formId(form.getId())
                .userId(submitter.getId())
                .username(submitter.getUsername())
                .timestamp(System.currentTimeMillis())
                .build();

        sendToFormTopic(form.getId(), message);

        log.debug("Notified collaborators of form submission: formId={}, userId={}",
                form.getId(), submitter.getId());
    }

    public void notifyFormArchived(Form form, User archiver) {
        GenericMessage message = GenericMessage.builder()
                .type("FORM_ARCHIVED")
                .formId(form.getId())
                .userId(archiver.getId())
                .username(archiver.getUsername())
                .timestamp(System.currentTimeMillis())
                .build();

        sendToFormTopic(form.getId(), message);

        log.debug("Notified collaborators of form archiving: formId={}, userId={}",
                form.getId(), archiver.getId());
    }

    public void notifyFormDeletion(Form form) {
        GenericMessage message = GenericMessage.builder()
                .type("FORM_DELETED")
                .formId(form.getId())
                .userId(form.getAdmin().getId())
                .username(form.getAdmin().getUsername())
                .timestamp(System.currentTimeMillis())
                .build();

        sendToFormTopic(form.getId(), message);

        log.debug("Notified collaborators of form deletion: formId={}", form.getId());
    }

    public void sendTemporaryFieldUpdate(Long formId, Long fieldId, String fieldName,
                                         String fieldType, String value, User user) {
        FieldUpdateMessage message = FieldUpdateMessage.builder()
                .type("FIELD_UPDATE")
                .formId(formId)
                .userId(user.getId())
                .username(user.getUsername())
                .fieldId(fieldId)
                .fieldName(fieldName)
                .fieldType(fieldType)
                .value(value)
                .isTemporary(true)
                .timestamp(System.currentTimeMillis())
                .build();

        sendToFormTopic(formId, message);
    }

    private void sendToFormTopic(Long formId, Object message) {
        messagingTemplate.convertAndSend(FORM_TOPIC_PREFIX + formId, message);
    }
}
