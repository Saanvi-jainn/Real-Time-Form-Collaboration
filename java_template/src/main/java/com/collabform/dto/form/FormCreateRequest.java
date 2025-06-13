package com.collabform.dto.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for form creation requests.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormCreateRequest {
    
    @NotBlank(message = "Form title is required")
    @Size(max = 255, message = "Form title must be less than 255 characters")
    private String title;
    
    @Size(max = 1000, message = "Form description must be less than 1000 characters")
    private String description;
    
    @Valid
    private List<FormFieldRequest> fields = new ArrayList<>();
}