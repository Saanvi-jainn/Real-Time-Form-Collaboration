package com.collabform.repository;

import com.collabform.model.FieldValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing FieldValue entities in the database.
 */
@Repository
public interface FieldValueRepository extends JpaRepository<FieldValue, Long> {
    /**
     * Find a specific field value by response ID and field ID.
     * 
     * @param responseId The ID of the response
     * @param fieldId The ID of the field
     * @return An Optional containing the field value if found, empty otherwise
     */
    Optional<FieldValue> findByResponseIdAndFieldId(Long responseId, Long fieldId);
    
    /**
     * Find all field values for a specific response.
     * 
     * @param responseId The ID of the response
     * @return A list of field values for the response
     */
    List<FieldValue> findByResponseId(Long responseId);
}