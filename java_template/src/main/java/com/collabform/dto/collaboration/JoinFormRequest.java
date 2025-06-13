package com.collabform.dto.collaboration;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for requesting to join a form using an access code.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinFormRequest {
    
    @NotBlank(message = "Access code is required")
    private String accessCode;
}