package com.foodordering.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodordering.security.ClientIpResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class EndpointRateLimitSecurityTest {

    private EndpointRateLimiter endpointRateLimiter;
    private LoginRateLimitService loginRateLimitService;
    private LoginRateLimitFilter filter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RateLimiterStore store = new InMemoryRateLimiterStore();
        endpointRateLimiter = new EndpointRateLimiter(store);
        loginRateLimitService = new LoginRateLimitService();
        ClientIpResolver clientIpResolver = new ClientIpResolver();

        filter = new LoginRateLimitFilter(
                loginRateLimitService,
                objectMapper,
                false,
                clientIpResolver,
                endpointRateLimiter
        );
    }

    @Test
    void testRegistrationRateLimit_EnforcesThreshold() throws Exception {
        String ip = "197.232.88.1";

        // Limit is 5 attempts
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/register");
            req.setRemoteAddr(ip);
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(req, res, chain);
            assertEquals(200, res.getStatus());
        }

        // 6th attempt should be blocked with 429
        MockHttpServletRequest blockedReq = new MockHttpServletRequest("POST", "/api/auth/register");
        blockedReq.setRemoteAddr(ip);
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(blockedReq, blockedRes, chain);
        assertEquals(429, blockedRes.getStatus());
        assertNotNull(blockedRes.getHeader("Retry-After"));
    }

    @Test
    void testForgotPasswordRateLimit_EnforcesThreshold() throws Exception {
        String ip = "197.232.88.2";

        // Limit is 3 attempts
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/forgot-password");
            req.setRemoteAddr(ip);
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(req, res, chain);
            assertEquals(200, res.getStatus());
        }

        // 4th attempt should be blocked with 429
        MockHttpServletRequest blockedReq = new MockHttpServletRequest("POST", "/api/auth/forgot-password");
        blockedReq.setRemoteAddr(ip);
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(blockedReq, blockedRes, chain);
        assertEquals(429, blockedRes.getStatus());
    }

    @Test
    void testCheckoutRateLimit_EnforcesThreshold() throws Exception {
        String ip = "197.232.88.3";

        // Limit is 15 requests
        for (int i = 0; i < 15; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/orders");
            req.setRemoteAddr(ip);
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(req, res, chain);
            assertEquals(200, res.getStatus());
        }

        // 16th attempt should be blocked with 429
        MockHttpServletRequest blockedReq = new MockHttpServletRequest("POST", "/api/orders");
        blockedReq.setRemoteAddr(ip);
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(blockedReq, blockedRes, chain);
        assertEquals(429, blockedRes.getStatus());
    }

    @Test
    void testRateLimitBypassAttemptWithSpoofedIpHeaders_IsBlocked() throws Exception {
        String realClientIp = "197.232.88.4";

        // Attacker sends 5 login attempts and tries to bypass rate limiting by rotating fake headers
        for (int i = 0; i < 5; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/auth/login");
            req.setRemoteAddr(realClientIp);
            req.addHeader("X-Forwarded-For", "malicious-injection-string-" + i);
            MockHttpServletResponse res = new MockHttpServletResponse();
            MockFilterChain chain = new MockFilterChain();

            filter.doFilter(req, res, chain);
            // ClientIpResolver detects invalid IP in header and falls back to remoteAddr
            assertEquals(200, res.getStatus());
        }

        // 6th attempt from the same client IP is blocked, regardless of header spoofing
        MockHttpServletRequest blockedReq = new MockHttpServletRequest("POST", "/api/auth/login");
        blockedReq.setRemoteAddr(realClientIp);
        blockedReq.addHeader("X-Forwarded-For", "another-fake-ip-bypass-attempt");
        MockHttpServletResponse blockedRes = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(blockedReq, blockedRes, chain);
        assertEquals(429, blockedRes.getStatus());
    }
}

