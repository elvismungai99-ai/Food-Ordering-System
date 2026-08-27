package com.foodordering.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    /*
     * Access token lifetime: 15 minutes (900,000 ms).
     * Short-lived for enhanced security.
     */
    private final long accessTokenExpirationMs;

    @org.springframework.beans.factory.annotation.Autowired
    public JwtUtil(
            @Value("${jwt.secret}")
            String secret,
            @Value("${jwt.access-token-expiration-ms:900000}")
            long accessTokenExpirationMs
    ) {
        this.secretKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
        this.accessTokenExpirationMs =
                accessTokenExpirationMs;
    }

    public JwtUtil(String secret) {
        this(secret, 900000L); // 15 minutes default for testing
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMs / 1000L;
    }

    // =====================================================
    // GENERATE ACCESS TOKEN
    // =====================================================

    public String generateAccessToken(
            UUID userId,
            String email,
            String role
    ) {
        String normalizedRole = normalizeRole(role);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", normalizedRole)
                .issuedAt(new Date())
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + accessTokenExpirationMs
                        )
                )
                .signWith(secretKey)
                .compact();
    }

    public String generateToken(
            UUID userId,
            String email,
            String role
    ) {
        return generateAccessToken(userId, email, role);
    }

    // =====================================================
    // EXTRACT ALL CLAIMS
    // =====================================================

    private Claims extractAllClaims(
            String token
    ) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // =====================================================
    // EXTRACT USER ID
    // =====================================================

    public UUID extractUserId(
            String token
    ) {
        String subject =
                extractAllClaims(token).getSubject();

        return UUID.fromString(subject);
    }

    // =====================================================
    // EXTRACT EMAIL
    // =====================================================

    public String extractEmail(
            String token
    ) {
        return extractAllClaims(token)
                .get("email", String.class);
    }

    // =====================================================
    // EXTRACT ROLE
    // =====================================================

    public String extractRole(
            String token
    ) {
        return extractAllClaims(token)
                .get("role", String.class);
    }

    // =====================================================
    // EXTRACT EXPIRATION DATE
    // =====================================================

    public Date extractExpiration(
            String token
    ) {
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExpired(
            String token
    ) {
        return extractExpiration(token)
                .before(new Date());
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }

        String normalized = role.trim().toUpperCase(Locale.ROOT);

        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        if (
                normalized.equals("RESTAURANT_ADMIN")
                || normalized.equals("RESTAURANT_OWNER")
                || normalized.equals("ADMIN_RESTAURANT")
        ) {
            return "OWNER";
        }

        return normalized;
    }

    // =====================================================
    // VALIDATE TOKEN
    // =====================================================

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {
        String email = extractEmail(token);

        return email != null
                && userDetails != null
                && email.equalsIgnoreCase(userDetails.getUsername())
                && !isTokenExpired(token)
                && userDetails.isEnabled()
                && userDetails.isAccountNonLocked();
    }
}
