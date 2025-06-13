package com.collabform.dto.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for form sharing response.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormShareResponse {
    private Long formId;
    private String accessCode;
    private String sharedWithUsername;
    private Long sharedWithUserId;
    private String message;
    private boolean success;
}