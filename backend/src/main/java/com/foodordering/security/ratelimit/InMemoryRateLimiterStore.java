package com.foodordering.security.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRateLimiterStore implements RateLimiterStore {

    private final ConcurrentHashMap<String, AttemptWindow> store = new ConcurrentHashMap<>();

    @Override
    public RateLimitResult checkRateLimit(String key, int maxRequests, Duration window) {
        Instant now = Instant.now();

        AttemptWindow current = store.compute(key, (k, existing) -> {
            if (existing == null) {
                return new AttemptWindow(1, now, window);
            }

            Instant windowEnd = existing.windowStartedAt().plus(existing.window());
            if (now.isAfter(windowEnd) || now.equals(windowEnd)) {
                return new AttemptWindow(1, now, window);
            }

            return new AttemptWindow(existing.count() + 1, existing.windowStartedAt(), existing.window());
        });

        Instant resetAt = current.windowStartedAt().plus(current.window());
        long retryAfterSeconds = Math.max(0, Duration.between(now, resetAt).toSeconds());
        boolean allowed = current.count() <= maxRequests;
        int remaining = Math.max(0, maxRequests - current.count());

        // Periodic light cleanup of old entries
        if (store.size() > 5000) {
            cleanExpiredEntries(now);
        }

        return new RateLimitResult(allowed, remaining, retryAfterSeconds);
    }

    @Override
    public void reset(String key) {
        store.remove(key);
    }

    private void cleanExpiredEntries(Instant now) {
        store.entrySet().removeIf(entry -> {
            Instant expiresAt = entry.getValue().windowStartedAt().plus(entry.getValue().window());
            return now.isAfter(expiresAt);
        });
    }

    private record AttemptWindow(int count, Instant windowStartedAt, Duration window) {
    }
}

