package com.collabform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing access permissions for a user to a form.
 */
@Entity
@Table(name = "form_access")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String accessCode;

    @Column(nullable = false)
    private LocalDateTime accessGranted = LocalDateTime.now();
}