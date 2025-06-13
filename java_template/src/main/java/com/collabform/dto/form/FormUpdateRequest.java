package com.collabform.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for form update requests.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormUpdateRequest {
    
    @Size(max = 255, message = "Form title must be less than 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Form description must be less than 1000 characters")
    private String description;
    
    private Boolean active;
    
    @Valid
    private List<FormFieldRequest> fields = new ArrayList<>();
    
    /**
     * Indicates if the fields should be completely replaced.
     * If false, the provided fields will be merged with existing fields.
     */
    private boolean replaceAllFields = false;
}