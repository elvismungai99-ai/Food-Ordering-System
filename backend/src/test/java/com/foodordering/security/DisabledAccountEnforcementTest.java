package com.foodordering.security;

import com.foodordering.User.entity.User;
import com.foodordering.User.repository.UserRepository;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisabledAccountEnforcementTest {

    @Mock
    private UserRepository userRepository;

    private JwtUtil jwtUtil;
    private CustomUserDetailsService userDetailsService;
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private final String secret = "test-secret-key-that-is-at-least-32-characters-long-123456";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtUtil = new JwtUtil(secret);
        userDetailsService = new CustomUserDetailsService(userRepository);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtUtil, userDetailsService);
    }

    @Test
    void testActiveUser_CanAuthenticateWithValidJwt() throws Exception {
        UUID userId = UUID.randomUUID();
        String email = "activeuser@example.com";

        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setPasswordHash("hashed_pw");
        user.setRole("CUSTOMER");
        user.setActive(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        String token = jwtUtil.generateToken(userId, email, "CUSTOMER");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/me");
        request.addHeader("Authorization", "Bearer " + token);
        request.setServletPath("/api/orders/me");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertEquals(200, response.getStatus());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(email, SecurityContextHolder.getContext().getAuthentication().getName());
    }

    @Test
    void testDisabledUser_ImmediatelyDeniedWithForbidden_EvenWithValidJwt() throws Exception {
        UUID userId = UUID.randomUUID();
        String email = "disableduser@example.com";

        User disabledUser = new User();
        disabledUser.setId(userId);
        disabledUser.setEmail(email);
        disabledUser.setPasswordHash("hashed_pw");
        disabledUser.setRole("CUSTOMER");
        disabledUser.setActive(false); // Account disabled!

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(disabledUser));

        // Generate token before disabling account
        String token = jwtUtil.generateToken(userId, email, "CUSTOMER");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders/me");
        request.addHeader("Authorization", "Bearer " + token);
        request.setServletPath("/api/orders/me");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // Access is immediately revoked with HTTP 403 Forbidden
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("This account is currently disabled"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testJwtUtil_isTokenValid_ReturnsFalseForDisabledUserDetails() {
        UUID userId = UUID.randomUUID();
        String email = "disabled@example.com";

        User disabledUser = new User();
        disabledUser.setId(userId);
        disabledUser.setEmail(email);
        disabledUser.setPasswordHash("hashed_pw");
        disabledUser.setRole("CUSTOMER");
        disabledUser.setActive(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(disabledUser));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtUtil.generateToken(userId, email, "CUSTOMER");

        assertFalse(userDetails.isEnabled());
        assertFalse(jwtUtil.isTokenValid(token, userDetails));
    }
}

