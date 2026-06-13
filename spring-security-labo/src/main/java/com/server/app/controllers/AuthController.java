package com.server.app.controllers;

import com.server.app.dto.auth.AuthResponse;
import com.server.app.dto.auth.LoginDto;
import com.server.app.dto.auth.UpdatePasswordDto;
import com.server.app.dto.auth.UpdateProfileDto;
import com.server.app.dto.user.UserCreateDto;
import com.server.app.entities.User;
import com.server.app.services.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody UserCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.signup(dto));
    }

    @GetMapping("/profile")
    public ResponseEntity<User> profile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(authService.profile(user.getId()));
    }

    @PutMapping("/update/profile")
    public ResponseEntity<User> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileDto dto
    ) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(authService.updateProfile(user.getId(), dto));
    }

    @PutMapping("/update/password")
    public ResponseEntity<Void> updatePassword(
            Authentication authentication,
            @Valid @RequestBody UpdatePasswordDto dto
    ) {
        User user = (User) authentication.getPrincipal();
        authService.updatePassword(user.getId(), dto);
        return ResponseEntity.ok().build();
    }
}