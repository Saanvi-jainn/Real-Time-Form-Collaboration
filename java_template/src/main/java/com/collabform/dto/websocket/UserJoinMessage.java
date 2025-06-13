package com.collabform.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * WebSocket message for user join events.
 * Sent when a user joins a form collaboration session.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class UserJoinMessage extends WebSocketMessage {
    private String userRole;
    
    public static UserJoinMessage create(Long formId, Long userId, String username, String userRole) {
        return UserJoinMessage.builder()
                .type("USER_JOIN")
                .formId(formId)
                .userId(userId)
                .username(username)
                .userRole(userRole)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}