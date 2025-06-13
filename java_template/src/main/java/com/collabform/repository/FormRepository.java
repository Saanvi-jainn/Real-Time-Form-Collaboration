package com.collabform.repository;

import com.collabform.model.Form;
import com.collabform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for accessing Form entities in the database.
 */
@Repository
public interface FormRepository extends JpaRepository<Form, Long> {
    /**
     * Find all forms created by a specific admin.
     * 
     * @param admin The user who is the admin of the forms
     * @return A list of forms created by the admin
     */
    List<Form> findByAdmin(User admin);
    
    /**
     * Find active forms created by a specific admin.
     * 
     * @param admin The user who is the admin of the forms
     * @param active The active status to filter by
     * @return A list of active forms created by the admin
     */
    List<Form> findByAdminAndActive(User admin, boolean active);
    
    /**
     * Search for forms by title containing the specified text.
     * 
     * @param title The title text to search for
     * @return A list of forms with matching titles
     */
    List<Form> findByTitleContainingIgnoreCase(String title);
    
    /**
     * Find forms that are accessible to a specific user through FormAccess.
     * 
     * @param userId The ID of the user
     * @return A list of forms accessible to the user
     */
    @Query("SELECT DISTINCT fa.form FROM FormAccess fa WHERE fa.user.id = :userId")
    List<Form> findFormsAccessibleToUser(Long userId);
}