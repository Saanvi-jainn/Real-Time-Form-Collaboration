package com.collabform.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * WebSocket message for successful lock acquisition.
 * Sent when a user successfully acquires a lock on a field.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LockAcquiredMessage extends WebSocketMessage {
    private Long fieldId;
    private String fieldName;
    private Long expiresAt; // Timestamp when the lock expires
    
    public static LockAcquiredMessage create(Long formId, Long userId, String username, Long fieldId, String fieldName, Long expiresAt) {
        return LockAcquiredMessage.builder()
                .type("LOCK_ACQUIRED")
                .formId(formId)
                .userId(userId)
                .username(username)
                .fieldId(fieldId)
                .fieldName(fieldName)
                .expiresAt(expiresAt)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}