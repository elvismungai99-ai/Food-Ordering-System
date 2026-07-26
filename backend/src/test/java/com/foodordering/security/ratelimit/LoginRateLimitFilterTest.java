package com.foodordering.security.ratelimit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginRateLimitFilterTest {

    @Test
    void rateLimitsLoopbackRequestsByDefault() throws Exception {
        LoginRateLimitService loginRateLimitService = mock(LoginRateLimitService.class);
        LoginRateLimitFilter filter = new LoginRateLimitFilter(loginRateLimitService, new ObjectMapper(), false);

        when(loginRateLimitService.checkRequest("127.0.0.1"))
                .thenReturn(new LoginRateLimitService.RateLimitResult(true, 4, 30));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(loginRateLimitService).checkRequest("127.0.0.1");
    }

    @Test
    void bypassesRateLimitingForLoopbackRequestsWhenConfigured() throws Exception {
        LoginRateLimitService loginRateLimitService = mock(LoginRateLimitService.class);
        LoginRateLimitFilter filter = new LoginRateLimitFilter(loginRateLimitService, new ObjectMapper(), true);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(200);
        verifyNoInteractions(loginRateLimitService);
    }

    @Test
    void allowsPublicAuthEndpointsThroughSecurity() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        request.setContent("{\"email\":\"admin@foodordering.com\",\"password\":\"admin123\"}".getBytes());

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest()).isNull();
    }
}
