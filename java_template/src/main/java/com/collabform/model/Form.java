package com.collabform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.UUID;

/**
 * Entity representing a form in the system.
 */
@Entity
@Table(name = "forms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder")
    private List<FormField> fields = new ArrayList<>();

    @OneToOne(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    private FormResponse response;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<FormAccess> accessEntries = new ArrayList<>();

    /**
     * Generates a unique share code for this form.
     * @return A unique share code
     */
    public String generateShareCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Adds a new field to the form.
     * @param field The field to add
     */
    public void addField(FormField field) {
        fields.add(field);
        field.setForm(this);
    }

    /**
     * Removes a field from the form.
     * @param fieldId The ID of the field to remove
     */
    public void removeField(Long fieldId) {
        fields.removeIf(field -> field.getId().equals(fieldId));
    }

    /**
     * Updates an existing field in the form.
     * @param updatedField The updated field data
     */
    public void updateField(FormField updatedField) {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getId().equals(updatedField.getId())) {
                fields.set(i, updatedField);
                break;
            }
        }
    }
}