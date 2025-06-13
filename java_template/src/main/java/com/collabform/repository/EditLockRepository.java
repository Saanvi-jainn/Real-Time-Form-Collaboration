package com.collabform.repository;

import com.collabform.model.EditLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for accessing EditLock entities in the database.
 */
@Repository
public interface EditLockRepository extends JpaRepository<EditLock, Long> {
    /**
     * Find a lock for a specific field in a specific response.
     * 
     * @param fieldId The ID of the field
     * @param responseId The ID of the response
     * @return An Optional containing the lock if found, empty otherwise
     */
    Optional<EditLock> findByFieldIdAndResponseId(Long fieldId, Long responseId);
    
    /**
     * Find all locks for a specific response.
     * 
     * @param responseId The ID of the response
     * @return A list of locks for the response
     */
    List<EditLock> findByResponseId(Long responseId);
    
    /**
     * Delete all locks held by a specific user for a specific response.
     * 
     * @param userId The ID of the user
     * @param responseId The ID of the response
     */
    void deleteByUserIdAndResponseId(Long userId, Long responseId);
    
    /**
     * Find all locks held by a specific user.
     * 
     * @param userId The ID of the user
     * @return A list of locks held by the user
     */
    List<EditLock> findByUserId(Long userId);
    
    /**
     * Find all locks that have expired.
     * 
     * @param now The current time
     * @return A list of expired locks
     */
    List<EditLock> findByExpiresAtBefore(LocalDateTime now);
}