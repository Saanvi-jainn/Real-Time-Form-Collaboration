package com.collabform.service;

import com.collabform.dto.UserDto;
import com.collabform.dto.auth.AuthResponse;
import com.collabform.dto.auth.LoginRequest;
import com.collabform.dto.auth.RegisterRequest;
import com.collabform.model.User;
import com.collabform.model.UserRole;
import com.collabform.repository.UserRepository;
import com.collabform.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service for user-related operations including authentication.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Register a new user.
     *
     * @param request Registration details
     * @return Authentication response with JWT token
     */
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username is already taken");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        // Determine role (default to USER if not specified or not ADMIN)
        UserRole role = UserRole.USER;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            role = UserRole.ADMIN;
        }
        
        // Create new user entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT token
        String jwt = tokenProvider.generateTokenFromUser(savedUser);

        return AuthResponse.builder()
                .token(jwt)
                .user(UserDto.fromUser(savedUser))
                .expiresIn(tokenProvider.getJwtExpirationMs())
                .build();
    }

    /**
     * Authenticate a user and generate a JWT token.
     *
     * @param request Login credentials
     * @return Authentication response with JWT token
     */
    public AuthResponse authenticateUser(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        User user = (User) authentication.getPrincipal();
        
        return AuthResponse.builder()
                .token(jwt)
                .user(UserDto.fromUser(user))
                .expiresIn(tokenProvider.getJwtExpirationMs())
                .build();
    }

    /**
     * Get a user by their ID.
     *
     * @param id User ID
     * @return Optional containing the user if found
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find a user by username or email.
     *
     * @param usernameOrEmail Username or email
     * @return Optional containing the user if found
     */
    public Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        return userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail));
    }

    /**
     * Get the currently authenticated user.
     *
     * @return The authenticated user
     * @throws IllegalStateException if no user is authenticated
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            throw new IllegalStateException("No authenticated user found");
        }
        return (User) authentication.getPrincipal();
    }
}