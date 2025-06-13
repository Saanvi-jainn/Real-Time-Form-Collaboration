package com.collabform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a field in a form.
 */
@Entity
@Table(name = "form_fields")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Column(nullable = false)
    private String fieldName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FieldType fieldType;

    /**
     * JSON string containing options for DROPDOWN, RADIO, CHECKBOX field types
     */
    @Column(length = 1000)
    private String fieldOptions;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private int displayOrder;
}