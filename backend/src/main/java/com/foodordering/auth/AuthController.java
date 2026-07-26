package com.foodordering.auth;

import com.foodordering.auth.dto.LoginRequest;
import com.foodordering.auth.dto.ForgotPasswordRequest;
import com.foodordering.auth.dto.PasswordResetResponse;
import com.foodordering.auth.dto.RegisterRequest;
import com.foodordering.auth.dto.ResetPasswordRequest;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService
    ) {
        this.authService =
                authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.login(
                        request
                )
        );
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid
            @RequestBody
            RegisterRequest request
    ) {

        AuthResponse response =
                authService.register(
                        request
                );

        return ResponseEntity
                .status(
                        HttpStatus.CREATED
                )
                .body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> forgotPassword(
            @Valid
            @RequestBody
            ForgotPasswordRequest request
    ) {

        return ResponseEntity.ok(
                authService.requestPasswordReset(
                        request
                )
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(
            @Valid
            @RequestBody
            ResetPasswordRequest request
    ) {

        return ResponseEntity.ok(
                authService.resetPassword(
                        request
                )
        );
    }
}
