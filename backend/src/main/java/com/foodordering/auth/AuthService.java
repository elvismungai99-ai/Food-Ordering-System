package com.foodordering.auth;

import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;

import com.foodordering.auth.dto.LoginRequest;
import com.foodordering.auth.dto.ForgotPasswordRequest;
import com.foodordering.auth.dto.PasswordResetResponse;
import com.foodordering.auth.dto.RegisterRequest;
import com.foodordering.auth.dto.ResetPasswordRequest;

import com.foodordering.common.exception.ConflictException;
import com.foodordering.common.exception.ForbiddenOperationException;

import com.foodordering.security.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final AuthenticationManager
            authenticationManager;

    private final PasswordEncoder
            passwordEncoder;

    private final JwtUtil jwtUtil;

    private final PasswordResetTokenRepository
            passwordResetTokenRepository;

    private final PasswordResetEmailService
            passwordResetEmailService;

    private final String frontendBaseUrl;

    private final SecureRandom secureRandom =
            new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordResetEmailService passwordResetEmailService,
            @Value("${app.frontend-base-url:http://localhost:5173}")
            String frontendBaseUrl
    ) {

        this.userRepository =
                userRepository;

        this.authenticationManager =
                authenticationManager;

        this.passwordEncoder =
                passwordEncoder;

        this.jwtUtil =
                jwtUtil;

        this.passwordResetTokenRepository =
                passwordResetTokenRepository;

        this.passwordResetEmailService =
                passwordResetEmailService;

        this.frontendBaseUrl =
                frontendBaseUrl;
    }

    // =====================================================
    // REGISTER
    // =====================================================

    public AuthResponse register(
            RegisterRequest request
    ) {

        // ---------------------------------------------
        // NORMALIZE EMAIL
        // ---------------------------------------------

        String email =
                request
                        .getEmail()
                        .trim()
                        .toLowerCase();

        // ---------------------------------------------
        // CHECK DUPLICATE EMAIL
        // ---------------------------------------------

        if (
                userRepository
                        .existsByEmail(email)
        ) {

            throw new ConflictException(
                    "An account with this email already exists"
            );
        }

        // ---------------------------------------------
        // VALIDATE REGISTRATION ROLE
        // ---------------------------------------------

        String role =
                normalizeApplicationRole(
                        request.getRole()
                );

        /*
         * Public registration only allows:
         *
         * CUSTOMER
         * OWNER
         *
         * SUPER_ADMIN must never be created
         * from the public registration page.
         */
        if (
                !"CUSTOMER".equals(role)
                && !"OWNER".equals(role)
        ) {

            throw new ForbiddenOperationException(
                    "This account role cannot be created through public registration"
            );
        }

        // ---------------------------------------------
        // CREATE USER
        // ---------------------------------------------

        User user =
                new User();

        String fullName =
                request
                        .getFullName()
                        .trim();

        user.setFullName(
                fullName
        );

        user.setFirstName(
                extractFirstName(
                        fullName
                )
        );

        user.setLastName(
                extractLastName(
                        fullName
                )
        );

        user.setEmail(
                email
        );

        /*
         * The current User entity does not expose a phoneNumber field,
         * so registration should not try to persist it here.
         *
         * If you later add phoneNumber to the database model,
         * you can persist it through a dedicated field in User.
         */

        /*
         * Never save the raw password.
         *
         * Your User entity uses passwordHash.
         */
        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        user.setRole(
                role
        );

        user.setActive(
                true
        );

        User savedUser =
                userRepository.save(
                        user
                );

        // ---------------------------------------------
        // GENERATE JWT
        // ---------------------------------------------

        String token =
                jwtUtil.generateToken(
                        savedUser.getId(),
                        savedUser.getEmail(),
                        savedUser.getRole()
                );

        // ---------------------------------------------
        // RETURN LOGIN DATA
        // ---------------------------------------------

        return new AuthResponse(
                token,
                savedUser.getId(),
                normalizeApplicationRole(
                        savedUser.getRole()
                ),
                extractFirstName(
                        savedUser.getFullName()
                )
        );
    }

    // =====================================================
    // LOGIN
    // =====================================================

    public AuthResponse login(
            LoginRequest request
    ) {

        String email =
                request
                        .getEmail()
                        .trim()
                        .toLowerCase();

        // ---------------------------------------------
        // AUTHENTICATE EMAIL + PASSWORD
        // ---------------------------------------------

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.getPassword()
                    )
            );

        } catch (
                AuthenticationException exception
        ) {

            /*
             * Do not reveal whether the email
             * or password was wrong.
             */
            throw new BadCredentialsException(
                    "Email or password is incorrect"
            );
        }

        // ---------------------------------------------
        // LOAD USER
        // ---------------------------------------------

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new BadCredentialsException(
                                        "Email or password is incorrect"
                                )
                        );

        String role =
                normalizeApplicationRole(
                        user.getRole()
                );

        // ---------------------------------------------
        // CHECK ACCOUNT STATUS
        // ---------------------------------------------

        if (!user.isActive()) {

            throw new ForbiddenOperationException(
                    "This account is currently disabled"
            );
        }

        // ---------------------------------------------
        // GENERATE JWT
        // ---------------------------------------------

        String token =
                jwtUtil.generateToken(
                        user.getId(),
                        user.getEmail(),
                        role
                );

        // ---------------------------------------------
        // RETURN RESPONSE
        // ---------------------------------------------

        return new AuthResponse(
                token,
                user.getId(),
                role,
                extractFirstName(
                        user.getFullName()
                )
        );
    }

    // =====================================================
    // FIRST NAME
    // =====================================================

    private String extractFirstName(
            String fullName
    ) {

        if (
                fullName == null
                || fullName.isBlank()
        ) {
            return "";
        }

        return fullName
                .trim()
                .split("\\s+")[0];
    }

    private String extractLastName(
            String fullName
    ) {

        if (
                fullName == null
                || fullName.isBlank()
        ) {
            return "";
        }

        String[] parts =
                fullName
                        .trim()
                        .split("\\s+");

        if (parts.length <= 1) {
            return "";
        }

        return String.join(
                " ",
                java.util.Arrays.copyOfRange(
                        parts,
                        1,
                        parts.length
                )
        );
    }

    // =====================================================
    // FORGOT PASSWORD
    // =====================================================

    @Transactional
    public PasswordResetResponse requestPasswordReset(
            ForgotPasswordRequest request
    ) {

        String email =
                request
                        .getEmail()
                        .trim()
                        .toLowerCase();

        userRepository
                .findByEmail(email)
                .filter(User::isActive)
                .ifPresent(user -> {
                    String rawToken =
                            generateResetToken();

                    PasswordResetToken resetToken =
                            new PasswordResetToken();

                    resetToken.setUser(
                            user
                    );

                    resetToken.setTokenHash(
                            hashToken(
                                    rawToken
                            )
                    );

                    resetToken.setExpiresAt(
                            LocalDateTime
                                    .now()
                                    .plusMinutes(30)
                    );

                    passwordResetTokenRepository.save(
                            resetToken
                    );

                    passwordResetEmailService
                            .sendPasswordResetEmail(
                                    user,
                                    buildResetLink(
                                            rawToken
                                    )
                            );
                });

        return new PasswordResetResponse(
                "If an account exists for that email, a password reset link has been sent."
        );
    }

    // =====================================================
    // RESET PASSWORD
    // =====================================================

    @Transactional
    public PasswordResetResponse resetPassword(
            ResetPasswordRequest request
    ) {

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByTokenHash(
                                hashToken(
                                        request.getToken()
                                )
                        )
                        .orElseThrow(() ->
                                invalidResetToken()
                        );

        if (
                resetToken.isUsed()
                || resetToken
                        .getExpiresAt()
                        .isBefore(
                                LocalDateTime.now()
                        )
        ) {
            throw invalidResetToken();
        }

        User user =
                resetToken.getUser();

        if (!user.isActive()) {
            throw invalidResetToken();
        }

        user.setPasswordHash(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        resetToken.setUsedAt(
                LocalDateTime.now()
        );

        userRepository.save(
                user
        );

        passwordResetTokenRepository.save(
                resetToken
        );

        return new PasswordResetResponse(
                "Your password has been reset. You can now log in with the new password."
        );
    }

    private String normalizeApplicationRole(
            String role
    ) {

        if (
                role == null
                || role.isBlank()
        ) {
            return "";
        }

        String normalized =
                role
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (
                normalized.startsWith(
                        "ROLE_"
                )
        ) {
            normalized =
                    normalized.substring(5);
        }

        if (
                normalized.equals(
                        "RESTAURANT_ADMIN"
                )
                || normalized.equals(
                        "RESTAURANT_OWNER"
                )
                || normalized.equals(
                        "ADMIN_RESTAURANT"
                )
        ) {
            return "OWNER";
        }

        return normalized;
    }

    private String generateResetToken() {
        byte[] bytes =
                new byte[32];

        secureRandom.nextBytes(
                bytes
        );

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                        bytes
                );
    }

    private String buildResetLink(
            String token
    ) {

        return frontendBaseUrl
                + "/reset-password?token="
                + token;
    }

    private String hashToken(
            String token
    ) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            token.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat
                    .of()
                    .formatHex(
                            hash
                    );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }

    private ResponseStatusException invalidResetToken() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This password reset link is invalid or has expired"
        );
    }
}
