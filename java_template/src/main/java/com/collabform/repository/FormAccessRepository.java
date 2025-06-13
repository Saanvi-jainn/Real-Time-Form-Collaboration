package com.collabform.repository;

import com.collabform.model.FormAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing FormAccess entities in the database.
 */
@Repository
public interface FormAccessRepository extends JpaRepository<FormAccess, Long> {
    /**
     * Find a form access entry by its access code.
     * 
     * @param accessCode The unique access code for the form
     * @return An Optional containing the form access if found, empty otherwise
     */
    Optional<FormAccess> findByAccessCode(String accessCode);
    
    /**
     * Find all access entries for a specific form.
     * 
     * @param formId The ID of the form
     * @return A list of access entries for the form
     */
    List<FormAccess> findByFormId(Long formId);
    
    /**
     * Find a form access entry by form ID and user ID.
     * 
     * @param formId The ID of the form
     * @param userId The ID of the user
     * @return An Optional containing the form access if found, empty otherwise
     */
    Optional<FormAccess> findByFormIdAndUserId(Long formId, Long userId);
    
    /**
     * Check if a user has access to a specific form.
     * 
     * @param formId The ID of the form
     * @param userId The ID of the user
     * @return true if the user has access, false otherwise
     */
    boolean existsByFormIdAndUserId(Long formId, Long userId);
}