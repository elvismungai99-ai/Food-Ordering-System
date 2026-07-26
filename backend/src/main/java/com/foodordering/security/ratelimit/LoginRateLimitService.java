package com.foodordering.security.ratelimit;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginRateLimitService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofSeconds(30);

    private final ConcurrentHashMap<String, AttemptWindow> attempts =
            new ConcurrentHashMap<>();

    public RateLimitResult checkRequest(String clientKey) {
        Instant now = Instant.now();

        AttemptWindow current = attempts.compute(clientKey, (key, existing) -> {
            if (existing == null) {
                return new AttemptWindow(1, now);
            }

            Instant windowEnd = existing.windowStartedAt().plus(WINDOW);

            if (now.isAfter(windowEnd) || now.equals(windowEnd)) {
                return new AttemptWindow(1, now);
            }

            return new AttemptWindow(existing.count() + 1, existing.windowStartedAt());
        });

        Instant resetAt = current.windowStartedAt().plus(WINDOW);
        long retryAfterSeconds = Math.max(0, Duration.between(now, resetAt).toSeconds());
        boolean allowed = current.count() <= MAX_ATTEMPTS;
        int remaining = Math.max(0, MAX_ATTEMPTS - current.count());

        return new RateLimitResult(allowed, remaining, retryAfterSeconds);
    }

    public void reset(String clientKey) {
        attempts.remove(clientKey);
    }

    private record AttemptWindow(int count, Instant windowStartedAt) {
    }

    public record RateLimitResult(boolean allowed, int remaining, long retryAfterSeconds) {
    }
}
