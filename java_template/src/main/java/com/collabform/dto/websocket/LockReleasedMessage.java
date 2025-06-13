package com.collabform.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * WebSocket message for field lock release events.
 * Sent when a user releases a lock on a field.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LockReleasedMessage extends WebSocketMessage {
    private Long fieldId;
    private String fieldName;
    
    public static LockReleasedMessage create(Long formId, Long userId, String username, Long fieldId, String fieldName) {
        return LockReleasedMessage.builder()
                .type("LOCK_RELEASED")
                .formId(formId)
                .userId(userId)
                .username(username)
                .fieldId(fieldId)
                .fieldName(fieldName)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}