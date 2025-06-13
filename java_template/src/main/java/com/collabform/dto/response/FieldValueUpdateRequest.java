package com.collabform.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating the value of a specific field in a form response.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FieldValueUpdateRequest {
    
    @NotNull(message = "Field ID is required")
    private Long fieldId;
    
    private String value;
    
    private Boolean releaseLock; // If true, release the lock on this field after updating
}