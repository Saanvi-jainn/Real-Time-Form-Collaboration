package com.collabform.repository;

import com.collabform.model.FormField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for accessing FormField entities in the database.
 */
@Repository
public interface FormFieldRepository extends JpaRepository<FormField, Long> {
    /**
     * Find all fields for a specific form.
     * 
     * @param formId The ID of the form
     * @return A list of fields belonging to the form
     */
    List<FormField> findByFormId(Long formId);
    
    /**
     * Delete all fields for a specific form.
     * 
     * @param formId The ID of the form
     */
    void deleteByFormId(Long formId);
    
    /**
     * Find all fields for a specific form, ordered by display order.
     * 
     * @param formId The ID of the form
     * @return An ordered list of fields belonging to the form
     */
    List<FormField> findByFormIdOrderByDisplayOrder(Long formId);
}