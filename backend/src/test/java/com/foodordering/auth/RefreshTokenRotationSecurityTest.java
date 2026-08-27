package com.foodordering.auth;

import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;
import com.foodordering.auth.dto.RefreshTokenRequest;
import com.foodordering.auth.dto.RefreshTokenResponse;
import com.foodordering.common.exception.ForbiddenOperationException;
import com.foodordering.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenRotationSecurityTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordResetEmailService passwordResetEmailService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private JwtUtil jwtUtil;
    private AuthService authService;

    private final String secret = "test-secret-key-32chars-minimum-abcdef123456";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secret, 900000L); // 15 minutes
        authService = new AuthService(
                userRepository,
                authenticationManager,
                passwordEncoder,
                jwtUtil,
                passwordResetTokenRepository,
                passwordResetEmailService,
                refreshTokenRepository,
                "http://localhost:5173",
                604800000L // 7 days
        );
    }

    private User createTestUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setFullName("John Doe");
        user.setRole("CUSTOMER");
        user.setActive(true);
        return user;
    }

    @Test
    void testRefreshToken_ValidToken_RotatesSuccessfully() {
        User user = createTestUser();
        String rawToken = "raw-refresh-token-123456";
        String tokenHash = authService.hashToken(rawToken);

        RefreshToken storedToken = new RefreshToken();
        storedToken.setId(UUID.randomUUID());
        storedToken.setUser(user);
        storedToken.setTokenHash(tokenHash);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        storedToken.setCreatedAt(LocalDateTime.now());

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(storedToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        RefreshTokenResponse response = authService.refreshToken(new RefreshTokenRequest(rawToken));

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals(rawToken, response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900L, response.getExpiresIn());

        // Verify old token was revoked and replaced
        assertTrue(storedToken.isRevoked());
        assertNotNull(storedToken.getReplacedByTokenHash());

        // Verify save was called for both old and new token
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void testRefreshToken_RevokedToken_TriggersBreachDetectionAndRevokesAll() {
        User user = createTestUser();
        String rawToken = "reused-compromised-token";
        String tokenHash = authService.hashToken(rawToken);

        RefreshToken alreadyRevokedToken = new RefreshToken();
        alreadyRevokedToken.setId(UUID.randomUUID());
        alreadyRevokedToken.setUser(user);
        alreadyRevokedToken.setTokenHash(tokenHash);
        alreadyRevokedToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        alreadyRevokedToken.setRevokedAt(LocalDateTime.now().minusHours(1)); // Revoked earlier!

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(alreadyRevokedToken));

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> authService.refreshToken(new RefreshTokenRequest(rawToken))
        );

        assertTrue(exception.getMessage().contains("reuse detected"));

        // Verify all user tokens were revoked immediately
        verify(refreshTokenRepository).revokeAllActiveTokensForUser(eq(user), any(LocalDateTime.class));
    }

    @Test
    void testRefreshToken_ExpiredToken_RejectsWithForbidden() {
        User user = createTestUser();
        String rawToken = "expired-token";
        String tokenHash = authService.hashToken(rawToken);

        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setId(UUID.randomUUID());
        expiredToken.setUser(user);
        expiredToken.setTokenHash(tokenHash);
        expiredToken.setExpiresAt(LocalDateTime.now().minusMinutes(5)); // Expired!

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(expiredToken));

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> authService.refreshToken(new RefreshTokenRequest(rawToken))
        );

        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    void testRefreshToken_DisabledUser_RejectsWithForbidden() {
        User disabledUser = createTestUser();
        disabledUser.setActive(false); // Disabled!

        String rawToken = "token-for-disabled-user";
        String tokenHash = authService.hashToken(rawToken);

        RefreshToken storedToken = new RefreshToken();
        storedToken.setId(UUID.randomUUID());
        storedToken.setUser(disabledUser);
        storedToken.setTokenHash(tokenHash);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(storedToken));

        ForbiddenOperationException exception = assertThrows(
                ForbiddenOperationException.class,
                () -> authService.refreshToken(new RefreshTokenRequest(rawToken))
        );

        assertTrue(exception.getMessage().contains("disabled"));
    }

    @Test
    void testLogout_RevokesRefreshToken() {
        User user = createTestUser();
        String rawToken = "logout-token";
        String tokenHash = authService.hashToken(rawToken);

        RefreshToken storedToken = new RefreshToken();
        storedToken.setId(UUID.randomUUID());
        storedToken.setUser(user);
        storedToken.setTokenHash(tokenHash);
        storedToken.setExpiresAt(LocalDateTime.now().plusDays(7));

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(storedToken));

        authService.logout(new RefreshTokenRequest(rawToken));

        assertTrue(storedToken.isRevoked());
        verify(refreshTokenRepository).save(storedToken);
    }
}

