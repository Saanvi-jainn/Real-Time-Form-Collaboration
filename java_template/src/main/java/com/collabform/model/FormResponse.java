package com.collabform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a collaborative response to a form.
 * Each form has one shared response that multiple users can edit.
 */
@Entity
@Table(name = "form_responses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "form_id", unique = true, nullable = false)
    private Form form;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResponseStatus status = ResponseStatus.DRAFT;

    @OneToMany(mappedBy = "response", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FieldValue> fieldValues = new ArrayList<>();

    /**
     * Updates a field value in this response.
     * @param fieldId The field ID to update
     * @param value The new value
     * @param user The user making the update
     */
    public void updateFieldValue(Long fieldId, String value, User user) {
        for (FieldValue fieldValue : fieldValues) {
            if (fieldValue.getField().getId().equals(fieldId)) {
                fieldValue.setValue(value, user);
                return;
            }
        }
        
        // If field value doesn't exist yet, create a new one
        FormField field = form.getFields().stream()
                .filter(f -> f.getId().equals(fieldId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Field not found"));
                
        FieldValue newValue = FieldValue.builder()
                .response(this)
                .field(field)
                .value(value)
                .lastUpdated(LocalDateTime.now())
                .lastUpdatedBy(user)
                .build();
                
        fieldValues.add(newValue);
    }
}