package com.foodordering.security.ratelimit;

import java.time.Duration;

public interface RateLimiterStore {

    RateLimitResult checkRateLimit(String key, int maxRequests, Duration window);

    void reset(String key);

    record RateLimitResult(boolean allowed, int remaining, long retryAfterSeconds) {
    }
}

