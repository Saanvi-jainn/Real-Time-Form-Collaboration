package com.collabform.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * WebSocket message for user leave events.
 * Sent when a user leaves a form collaboration session.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
public class UserLeaveMessage extends WebSocketMessage {
    
    public static UserLeaveMessage create(Long formId, Long userId, String username) {
        return UserLeaveMessage.builder()
                .type("USER_LEAVE")
                .formId(formId)
                .userId(userId)
                .username(username)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}