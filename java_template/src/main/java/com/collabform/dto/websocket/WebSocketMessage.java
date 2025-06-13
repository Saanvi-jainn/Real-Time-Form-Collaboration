package com.collabform.dto.websocket;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Base abstract class for all WebSocket messages.
 * Uses Jackson's polymorphic type handling to properly deserialize messages.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = FieldUpdateMessage.class, name = "FIELD_UPDATE"),
        @JsonSubTypes.Type(value = UserJoinMessage.class, name = "USER_JOIN"),
        @JsonSubTypes.Type(value = UserLeaveMessage.class, name = "USER_LEAVE"),
        @JsonSubTypes.Type(value = LockRequestMessage.class, name = "LOCK_REQUEST"),
        @JsonSubTypes.Type(value = LockAcquiredMessage.class, name = "LOCK_ACQUIRED"),
        @JsonSubTypes.Type(value = LockReleasedMessage.class, name = "LOCK_RELEASED"),
        @JsonSubTypes.Type(value = GenericMessage.class, name = "GENERIC_MESSAGE") // ✅ Add this
})
public abstract class WebSocketMessage {
    private String type;
    private Long formId;
    private Long userId;
    private String username;
    private Long timestamp;
}
