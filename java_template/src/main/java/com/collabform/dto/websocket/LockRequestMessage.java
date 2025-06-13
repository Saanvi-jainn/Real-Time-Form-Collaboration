package com.collabform.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * WebSocket message for field lock requests.
 * Sent when a user wants to lock a field for editing.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class LockRequestMessage extends WebSocketMessage {
    private Long fieldId;
    private String fieldName;
    private boolean force; // Whether to force acquisition even if already locked
    
    public static LockRequestMessage create(Long formId, Long userId, String username, Long fieldId, String fieldName) {
        return LockRequestMessage.builder()
                .type("LOCK_REQUEST")
                .formId(formId)
                .userId(userId)
                .username(username)
                .fieldId(fieldId)
                .fieldName(fieldName)
                .force(false)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}