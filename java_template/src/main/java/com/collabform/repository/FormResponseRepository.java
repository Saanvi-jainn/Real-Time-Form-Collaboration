package com.collabform.repository;

import com.collabform.model.FormResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for accessing FormResponse entities in the database.
 */
@Repository
public interface FormResponseRepository extends JpaRepository<FormResponse, Long> {
    /**
     * Find the response for a specific form.
     * Each form has exactly one response in this system.
     * 
     * @param formId The ID of the form
     * @return An Optional containing the response if found, empty otherwise
     */
    Optional<FormResponse> findByFormId(Long formId);
}