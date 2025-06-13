package com.collabform.dto.form;

import com.collabform.model.FieldType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for form field requests (creation or update).
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormFieldRequest {
    
    private Long id; // null for new fields, populated for existing fields
    
    @NotBlank(message = "Field name is required")
    private String fieldName;
    
    @NotNull(message = "Field type is required")
    private FieldType fieldType;
    
    private String fieldOptions; // JSON string for options (DROPDOWN, RADIO, CHECKBOX)
    
    private boolean required;
    
    private Integer displayOrder;
}