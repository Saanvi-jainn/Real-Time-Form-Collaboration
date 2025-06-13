package com.collabform.controller;

import com.collabform.dto.auth.AuthResponse;
import com.collabform.dto.auth.LoginRequest;
import com.collabform.dto.auth.RegisterRequest;
import com.collabform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Slf4j
public class PublicController {

    private final UserService userService;

    @GetMapping("/check")
    public ResponseEntity<?> checkAuth() {

        return new ResponseEntity<>("Authentication is working!", HttpStatus.OK);
    }
}