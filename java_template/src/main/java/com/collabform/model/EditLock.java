package com.collabform.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a lock on a form field during collaborative editing.
 * This prevents multiple users from editing the same field simultaneously.
 */
@Entity
@Table(name = "edit_locks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "field_id", nullable = false)
    private FormField field;

    @ManyToOne
    @JoinColumn(name = "response_id", nullable = false)
    private FormResponse response;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime lockTime;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Checks if this lock has expired.
     * @return true if the lock has expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Refreshes the lock expiry time.
     * Typically called when a user continues editing a field.
     */
    public void refresh() {
        this.expiresAt = LocalDateTime.now().plusSeconds(30);
    }

    /**
     * Explicitly releases the lock.
     * This sets the expiry time to now, effectively releasing the lock.
     */
    public void release() {
        this.expiresAt = LocalDateTime.now();
    }
}