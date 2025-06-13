package com.collabform.dto.response;

import com.collabform.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for field value responses.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldValueResponse {
    private Long id;
    private Long fieldId;
    private String fieldName;
    private String fieldType;
    private String value;
    private LocalDateTime lastUpdated;
    private UserDto lastUpdatedBy;
    private boolean isLocked;
    private UserDto lockedBy;
    
    /**
     * Convert a FieldValue entity to a FieldValueResponse DTO.
     *
     * @param fieldValue The field value entity
     * @return A FieldValueResponse DTO
     */
    public static FieldValueResponse fromEntity(com.collabform.model.FieldValue fieldValue) {
        return FieldValueResponse.builder()
                .id(fieldValue.getId())
                .fieldId(fieldValue.getField().getId())
                .fieldName(fieldValue.getField().getFieldName())
                .fieldType(fieldValue.getField().getFieldType().toString())
                .value(fieldValue.getValue())
                .lastUpdated(fieldValue.getLastUpdated())
                .lastUpdatedBy(fieldValue.getLastUpdatedBy() != null ? 
                        UserDto.fromUser(fieldValue.getLastUpdatedBy()) : null)
                .build();
    }
}