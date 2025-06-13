package com.collabform.dto.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for sharing a form with another user.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormShareRequest {
    
    @NotEmpty(message = "Username or email is required")
    private String usernameOrEmail;
    
    private String customMessage; // Optional message to include with the share invitation
}