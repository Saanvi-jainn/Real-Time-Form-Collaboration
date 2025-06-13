package com.collabform.controller;

import com.collabform.dto.auth.AuthResponse;
import com.collabform.dto.auth.LoginRequest;
import com.collabform.dto.auth.RegisterRequest;
import com.collabform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication-related endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    /**
     * Register a new user.
     *
     * @param request Registration details
     * @return Authentication response with JWT token
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registering user: {}", request.getUsername());
        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Authenticate a user and get a JWT token.
     *
     * @param request Login credentials
     * @return Authentication response with JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());
        AuthResponse response = userService.authenticateUser(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Check if authentication is working.
     *
     * @return Simple success message
     */
    @GetMapping("/check")
    public ResponseEntity<String> checkAuth() {
        return ResponseEntity.ok("Authentication is working!");
    }
}