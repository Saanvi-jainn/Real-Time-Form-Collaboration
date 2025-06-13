package com.collabform.security;

import com.collabform.model.User;
import com.collabform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * Loads user details from the database for authentication.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load a user by username for authentication.
     *
     * @param username The username or email to load
     * @return The user details
     * @throws UsernameNotFoundException If the user is not found
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find the user by username or email
        return userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with username or email: " + username)));
    }

    /**
     * Load a user by ID.
     *
     * @param id The user ID to load
     * @return The user entity
     * @throws UsernameNotFoundException If the user is not found
     */
    @Transactional(readOnly = true)
    public User loadUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }
}