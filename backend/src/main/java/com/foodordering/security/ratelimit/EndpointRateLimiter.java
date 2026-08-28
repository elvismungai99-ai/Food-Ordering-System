package com.foodordering.security.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class EndpointRateLimiter {

    private final RateLimiterStore rateLimiterStore;

    public EndpointRateLimiter(RateLimiterStore rateLimiterStore) {
        this.rateLimiterStore = rateLimiterStore;
    }

    public RateLimiterStore.RateLimitResult checkRateLimit(
            String endpointType,
            String clientIdentifier,
            int maxRequests,
            Duration window
    ) {
        String key = "rl:" + endpointType + ":" + clientIdentifier;
        return rateLimiterStore.checkRateLimit(key, maxRequests, window);
    }

    public void reset(String endpointType, String clientIdentifier) {
        String key = "rl:" + endpointType + ":" + clientIdentifier;
        rateLimiterStore.reset(key);
    }
}

