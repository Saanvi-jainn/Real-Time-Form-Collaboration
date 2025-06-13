package com.collabform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a field value in a form response.
 */
@Entity
@Table(name = "field_values")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "response_id", nullable = false)
    private FormResponse response;

    @ManyToOne
    @JoinColumn(name = "field_id", nullable = false)
    private FormField field;

    @Column(name = "`value`", length = 4000) // Using backticks for H2
    private String value;

    private LocalDateTime lastUpdated;

    @ManyToOne
    @JoinColumn(name = "last_updated_by")
    private User lastUpdatedBy;

    /**
     * Sets the value and updates the last updated information.
     * @param value The new value
     * @param user The user making the update
     */
    public void setValue(String value, User user) {
        this.value = value;
        this.lastUpdated = LocalDateTime.now();
        this.lastUpdatedBy = user;
    }
}