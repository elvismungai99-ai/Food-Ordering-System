package com.foodordering.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class ClientIpResolverTest {

    private ClientIpResolver clientIpResolver;

    @BeforeEach
    void setUp() {
        clientIpResolver = new ClientIpResolver();
    }

    @Test
    void testResolveClientIp_FromCloudflareHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("CF-Connecting-IP", "197.232.88.10");
        request.setRemoteAddr("10.0.0.1");

        String ip = clientIpResolver.resolveClientIp(request);
        assertEquals("197.232.88.10", ip);
    }

    @Test
    void testResolveClientIp_FromXForwardedForMultiHop() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "197.232.88.10, 10.0.0.2, 127.0.0.1");
        request.setRemoteAddr("10.0.0.1");

        String ip = clientIpResolver.resolveClientIp(request);
        assertEquals("197.232.88.10", ip);
    }

    @Test
    void testResolveClientIp_FromXRealIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "41.89.24.5");
        request.setRemoteAddr("127.0.0.1");

        String ip = clientIpResolver.resolveClientIp(request);
        assertEquals("41.89.24.5", ip);
    }

    @Test
    void testResolveClientIp_FallbackToRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.50");

        String ip = clientIpResolver.resolveClientIp(request);
        assertEquals("192.168.1.50", ip);
    }

    @Test
    void testResolveClientIp_InvalidOrInjectedHeader_FallsBackSafely() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "malicious_sql_injection' OR 1=1--");
        request.setRemoteAddr("197.232.88.10");

        String ip = clientIpResolver.resolveClientIp(request);
        assertEquals("197.232.88.10", ip);
    }
}

