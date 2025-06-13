package com.collabform.dto.form;

import com.collabform.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for form responses.
 * Note: This is not the same as FormResponse entity, but rather
 * a DTO representing a form's data to be sent to the client.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormResponse {
    private Long id;
    private String title;
    private String description;
    private UserDto admin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;
    private List<FormFieldResponse> fields = new ArrayList<>();
    private Long responseId; // ID of the associated form response entity
    private String responseStatus; // DRAFT, SUBMITTED, or ARCHIVED
    private List<UserDto> collaborators = new ArrayList<>();
    
    /**
     * Convert a Form entity to a FormResponse DTO.
     *
     * @param form The form entity
     * @param includeFields Whether to include field information
     * @param includeResponse Whether to include response information
     * @param includeCollaborators Whether to include collaborator information
     * @return A FormResponse DTO
     */
    public static FormResponse fromEntity(
            com.collabform.model.Form form,
            boolean includeFields,
            boolean includeResponse,
            boolean includeCollaborators) {
        
        FormResponseBuilder builder = FormResponse.builder()
                .id(form.getId())
                .title(form.getTitle())
                .description(form.getDescription())
                .admin(UserDto.fromUser(form.getAdmin()))
                .createdAt(form.getCreatedAt())
                .updatedAt(form.getUpdatedAt())
                .active(form.isActive());
        
        if (includeFields) {
            List<FormFieldResponse> fieldResponses = new ArrayList<>();
            form.getFields().forEach(field -> 
                fieldResponses.add(FormFieldResponse.fromEntity(field))
            );
            builder.fields(fieldResponses);
        }
        
        if (includeResponse && form.getResponse() != null) {
            builder.responseId(form.getResponse().getId());
            builder.responseStatus(form.getResponse().getStatus().toString());
        }
        
        if (includeCollaborators) {
            List<UserDto> collaboratorDtos = new ArrayList<>();
            form.getAccessEntries().forEach(access -> 
                collaboratorDtos.add(UserDto.fromUser(access.getUser()))
            );
            builder.collaborators(collaboratorDtos);
        }
        
        return builder.build();
    }
}