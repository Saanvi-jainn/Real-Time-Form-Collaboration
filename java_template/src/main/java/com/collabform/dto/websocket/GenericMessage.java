package com.collabform.dto.websocket;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

/**
 * Generic WebSocket message for types like USER_ADDED, FORM_UPDATED, etc.
 */
@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class GenericMessage extends WebSocketMessage {
    // You can optionally add more fields here if needed
}
