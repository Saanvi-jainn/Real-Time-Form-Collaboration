package com.collabform.repository;

import com.collabform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for accessing User entities in the database.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Find a user by their username.
     * 
     * @param username The username to search for
     * @return An Optional containing the user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find a user by their email address.
     * 
     * @param email The email to search for
     * @return An Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a username exists.
     * 
     * @param username The username to check
     * @return true if the username exists, false otherwise
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if an email exists.
     * 
     * @param email The email to check
     * @return true if the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}