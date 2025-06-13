package com.collabform.dto.form;

import com.collabform.model.FieldType;
import com.collabform.model.FormField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for form field responses.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormFieldResponse {
    private Long id;
    private String fieldName;
    private FieldType fieldType;
    private String fieldOptions;
    private boolean required;
    private int displayOrder;
    private String currentValue; // Current value if this field has been filled
    private boolean isLocked; // Whether this field is currently being edited by someone
    private Long lockedByUserId; // User ID of the person editing this field (if locked)
    private String lockedByUsername; // Username of the person editing this field (if locked)
    
    /**
     * Convert a FormField entity to a FormFieldResponse DTO.
     *
     * @param field The form field entity
     * @return A FormFieldResponse DTO
     */
    public static FormFieldResponse fromEntity(FormField field) {
        return FormFieldResponse.builder()
                .id(field.getId())
                .fieldName(field.getFieldName())
                .fieldType(field.getFieldType())
                .fieldOptions(field.getFieldOptions())
                .required(field.isRequired())
                .displayOrder(field.getDisplayOrder())
                .build();
    }
}