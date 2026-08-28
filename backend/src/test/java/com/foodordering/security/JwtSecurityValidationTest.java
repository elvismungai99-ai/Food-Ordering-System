package com.foodordering.security;

import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtSecurityValidationTest {

    @Mock
    private UserRepository userRepository;

    private JwtUtil jwtUtil;
    private CustomUserDetailsService userDetailsService;
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String secret = "super-secure-test-jwt-secret-key-that-is-at-least-64-bytes-long-for-hmac-sha512";
    private final String attackerSecret = "an-attacker-crafted-secret-key-that-does-not-match-the-server-key-123456";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtUtil = new JwtUtil(secret);
        userDetailsService = new CustomUserDetailsService(userRepository);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Test
    void testExpiredJwt_IsRejected() {
        UUID userId = UUID.randomUUID();
        String email = "expired@example.com";

        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        // Create a token expired 1 hour ago
        String expiredToken = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", "CUSTOMER")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();

        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setRole("CUSTOMER");
        user.setActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertFalse(jwtUtil.isTokenValid(expiredToken, userDetails));
    }

    @Test
    void testTamperedJwtSignature_IsRejected() {
        UUID userId = UUID.randomUUID();
        String email = "victim@example.com";

        // Signed with attacker's secret key
        SecretKey attackerKey = Keys.hmacShaKeyFor(attackerSecret.getBytes(StandardCharsets.UTF_8));
        String forgedToken = Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", "SUPER_ADMIN") // Privilege escalation attempt
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(attackerKey)
                .compact();

        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setRole("CUSTOMER");
        user.setActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertFalse(jwtUtil.isTokenValid(forgedToken, userDetails));
    }

    @Test
    void testTamperedJwtPayload_IsRejectedByFilter() throws Exception {
        UUID userId = UUID.randomUUID();
        String email = "tampered@example.com";

        String validToken = jwtUtil.generateToken(userId, email, "CUSTOMER");
        // Tamper with the token string (e.g. change last characters of payload)
        String tamperedToken = validToken.substring(0, validToken.lastIndexOf('.') - 3) + "xyz" + validToken.substring(validToken.lastIndexOf('.'));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        request.addHeader("Authorization", "Bearer " + tamperedToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Tampered token must NOT establish a security context
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testRoleChangedInDatabaseAfterTokenCreation_LoadsLiveRoleFromDatabase() {
        UUID userId = UUID.randomUUID();
        String email = "user@example.com";

        // Token was created with OWNER role claim
        String token = jwtUtil.generateToken(userId, email, "OWNER");

        // But in the database, the user was demoted or changed to CUSTOMER
        User userInDb = new User();
        userInDb.setId(userId);
        userInDb.setEmail(email);
        userInDb.setPasswordHash("hashed");
        userInDb.setRole("CUSTOMER"); // Current database role
        userInDb.setActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userInDb));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Security check verifies that authorities are loaded from database UserDetails
        assertTrue(jwtUtil.isTokenValid(token, userDetails));
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER")));
        assertFalse(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER")));
    }

    @Test
    void testMalformedAuthorizationHeader_DoesNotAuthenticate() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        request.addHeader("Authorization", "Bearer invalid-garbage-token-string");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}

