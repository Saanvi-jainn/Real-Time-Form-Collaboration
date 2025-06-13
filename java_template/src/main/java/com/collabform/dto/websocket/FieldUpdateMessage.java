package com.collabform.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * WebSocket message for field value updates.
 * Sent when a user updates the value of a field.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class FieldUpdateMessage extends WebSocketMessage {
    private Long fieldId;
    private String fieldName;
    private String value;
    private String fieldType;
    
    // This is used when a user is typing but hasn't committed the change
    private boolean isTemporary;
}