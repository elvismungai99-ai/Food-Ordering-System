package com.foodordering.auth;

import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;

import com.foodordering.auth.dto.LoginRequest;
import com.foodordering.auth.dto.ForgotPasswordRequest;
import com.foodordering.auth.dto.PasswordResetResponse;
import com.foodordering.auth.dto.RefreshTokenRequest;
import com.foodordering.auth.dto.RefreshTokenResponse;
import com.foodordering.auth.dto.RegisterRequest;
import com.foodordering.auth.dto.ResetPasswordRequest;

import com.foodordering.common.exception.ConflictException;
import com.foodordering.common.exception.ForbiddenOperationException;

import com.foodordering.security.JwtUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Base64;
import java.util.UUID;

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

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final PasswordResetTokenRepository passwordResetTokenRepository;

    private final PasswordResetEmailService passwordResetEmailService;

    private final RefreshTokenRepository refreshTokenRepository;

    private final String frontendBaseUrl;

    private final long refreshTokenExpirationMs;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordResetEmailService passwordResetEmailService,
            RefreshTokenRepository refreshTokenRepository,
            @Value("${app.frontend-base-url:http://localhost:5173}")
            String frontendBaseUrl,
            @Value("${jwt.refresh-token-expiration-ms:604800000}")
            long refreshTokenExpirationMs
    ) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordResetEmailService = passwordResetEmailService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.frontendBaseUrl = frontendBaseUrl;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    // =====================================================
    // REGISTER
    // =====================================================

    @Transactional
    public AuthResponse register(
            RegisterRequest request
    ) {
        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(
                    "An account with this email already exists"
            );
        }

        String role = normalizeApplicationRole(request.getRole());

        if (!"CUSTOMER".equals(role) && !"OWNER".equals(role)) {
            throw new ForbiddenOperationException(
                    "This account role cannot be created through public registration"
            );
        }

        User user = new User();
        String fullName = request.getFullName().trim();
        user.setFullName(fullName);
        user.setFirstName(extractFirstName(fullName));
        user.setLastName(extractLastName(fullName));
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // Generate short-lived access token + long-lived rotating refresh token
        String accessToken = jwtUtil.generateAccessToken(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
        String refreshToken = createAndSaveRefreshToken(savedUser);

        return new AuthResponse(
                accessToken,
                refreshToken,
                savedUser.getId(),
                normalizeApplicationRole(savedUser.getRole()),
                extractFirstName(savedUser.getFullName()),
                jwtUtil.getAccessTokenExpirationSeconds()
        );
    }

    // =====================================================
    // LOGIN
    // =====================================================

    @Transactional
    public AuthResponse login(
            LoginRequest request
    ) {
        String email = request.getEmail().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.getPassword()
                    )
            );
        } catch (AuthenticationException exception) {
            throw new BadCredentialsException(
                    "Email or password is incorrect"
            );
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Email or password is incorrect"
                        )
                );

        String role = normalizeApplicationRole(user.getRole());

        if (!user.isActive()) {
            throw new ForbiddenOperationException(
                    "This account is currently disabled"
            );
        }

        String accessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                role
        );
        String refreshToken = createAndSaveRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                role,
                extractFirstName(user.getFullName()),
                jwtUtil.getAccessTokenExpirationSeconds()
        );
    }

    // =====================================================
    // REFRESH TOKEN WITH ROTATION & REUSE DETECTION
    // =====================================================

    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw new ForbiddenOperationException("Refresh token is required");
        }

        String rawToken = request.getRefreshToken().trim();
        String tokenHash = hashToken(rawToken);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ForbiddenOperationException("Invalid or unrecognized refresh token"));

        // 1. REUSE DETECTION: If an already-revoked token is used, suspect breach and revoke all active tokens
        if (storedToken.isRevoked()) {
            refreshTokenRepository.revokeAllActiveTokensForUser(storedToken.getUser(), LocalDateTime.now());
            throw new ForbiddenOperationException("Suspicious activity: Refresh token reuse detected. Please log in again.");
        }

        // 2. EXPIRATION CHECK
        if (storedToken.isExpired()) {
            throw new ForbiddenOperationException("Refresh token has expired. Please log in again.");
        }

        // 3. USER ACTIVE CHECK
        User user = storedToken.getUser();
        if (!user.isActive()) {
            throw new ForbiddenOperationException("This account is currently disabled");
        }

        // 4. ROTATION: Invalidate old token and issue fresh token pair
        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        String newRawRefreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String newHash = hashToken(newRawRefreshToken);

        storedToken.setRevokedAt(LocalDateTime.now());
        storedToken.setReplacedByTokenHash(newHash);
        refreshTokenRepository.save(storedToken);

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUser(user);
        newRefreshToken.setTokenHash(newHash);
        newRefreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs)));
        refreshTokenRepository.save(newRefreshToken);

        String newAccessToken = jwtUtil.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole()
        );

        return new RefreshTokenResponse(
                newAccessToken,
                newRawRefreshToken,
                jwtUtil.getAccessTokenExpirationSeconds()
        );
    }

    // =====================================================
    // LOGOUT & REVOCATION
    // =====================================================

    @Transactional
    public void logout(RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            String tokenHash = hashToken(request.getRefreshToken().trim());
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            });
        }
    }

    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        if (userId != null) {
            userRepository.findById(userId).ifPresent(user ->
                    refreshTokenRepository.revokeAllActiveTokensForUser(user, LocalDateTime.now())
            );
        }
    }

    private String createAndSaveRefreshToken(User user) {
        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs)));
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    // =====================================================
    // FORGOT PASSWORD
    // =====================================================

    @Transactional
    public PasswordResetResponse requestPasswordReset(
            ForgotPasswordRequest request
    ) {
        String email = request.getEmail().trim().toLowerCase();

        userRepository
                .findByEmail(email)
                .filter(User::isActive)
                .ifPresent(user -> {
                    String rawToken = generateResetToken();

                    PasswordResetToken resetToken = new PasswordResetToken();
                    resetToken.setUser(user);
                    resetToken.setTokenHash(hashToken(rawToken));
                    resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));

                    PasswordResetToken savedToken = passwordResetTokenRepository.save(resetToken);

                    passwordResetEmailService.sendPasswordResetEmail(
                            user,
                            buildResetLink(rawToken),
                            savedToken.getId()
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
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHash(hashToken(request.getToken()))
                .orElseThrow(this::invalidResetToken);

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw invalidResetToken();
        }

        User user = resetToken.getUser();

        if (!user.isActive()) {
            throw invalidResetToken();
        }

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        resetToken.setUsedAt(LocalDateTime.now());

        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);

        // Security: Revoke all existing active refresh tokens on password reset
        refreshTokenRepository.revokeAllActiveTokensForUser(user, LocalDateTime.now());

        return new PasswordResetResponse(
                "Your password has been reset. You can now log in with the new password."
        );
    }

    private String normalizeApplicationRole(String role) {
        if (role == null || role.isBlank()) {
            return "";
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        if (
                normalized.equals("RESTAURANT_ADMIN")
                || normalized.equals("RESTAURANT_OWNER")
                || normalized.equals("ADMIN_RESTAURANT")
        ) {
            return "OWNER";
        }

        return normalized;
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }

        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 0 ? parts[0] : "";
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }

        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            return parts[parts.length - 1];
        }

        return "";
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildResetLink(String token) {
        return frontendBaseUrl + "/reset-password?token=" + token;
    }

    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", exception);
        }
    }

    private ResponseStatusException invalidResetToken() {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid or expired reset token"
        );
    }
}
