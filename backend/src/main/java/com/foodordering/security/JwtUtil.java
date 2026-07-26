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
     * Token expires after 24 hours.
     */
    private static final long EXPIRATION_TIME =
            1000L * 60 * 60 * 24;

    public JwtUtil(
            @Value("${jwt.secret}")
            String secret
    ) {

        this.secretKey =
                Keys.hmacShaKeyFor(
                        secret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }

    // =====================================================
    // GENERATE TOKEN
    // =====================================================

    public String generateToken(
            UUID userId,
            String email,
            String role
    ) {

        String normalizedRole = normalizeRole(role);

        return Jwts.builder()

                /*
                 * Store user ID as JWT subject.
                 */
                .subject(
                        userId.toString()
                )

                /*
                 * Store email in the token.
                 */
                .claim(
                        "email",
                        email
                )

                /*
                 * Store application role.
                 *
                 * Examples:
                 * CUSTOMER
                 * OWNER
                 * SUPER_ADMIN
                 */
                .claim(
                        "role",
                        normalizedRole
                )

                .issuedAt(
                        new Date()
                )

                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION_TIME
                        )
                )

                .signWith(
                        secretKey
                )

                .compact();
    }

    // =====================================================
    // EXTRACT ALL CLAIMS
    // =====================================================

    private Claims extractAllClaims(
            String token
    ) {

        return Jwts.parser()

                .verifyWith(
                        secretKey
                )

                .build()

                .parseSignedClaims(
                        token
                )

                .getPayload();
    }

    // =====================================================
    // EXTRACT USER ID
    // =====================================================

    public UUID extractUserId(
            String token
    ) {

        String subject =
                extractAllClaims(
                        token
                )
                        .getSubject();

        return UUID.fromString(
                subject
        );
    }

    // =====================================================
    // EXTRACT EMAIL
    // =====================================================

    public String extractEmail(
            String token
    ) {

        return extractAllClaims(
                token
        )
                .get(
                        "email",
                        String.class
                );
    }

    // =====================================================
    // EXTRACT ROLE
    // =====================================================

    public String extractRole(
            String token
    ) {

        return extractAllClaims(
                token
        )
                .get(
                        "role",
                        String.class
                );
    }

    // =====================================================
    // EXTRACT EXPIRATION DATE
    // =====================================================

    public Date extractExpiration(
            String token
    ) {

        return extractAllClaims(
                token
        )
                .getExpiration();
    }

    // =====================================================
    // CHECK TOKEN EXPIRATION
    // =====================================================

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

    public boolean isTokenExpired(
            String token
    ) {

        return extractExpiration(
                token
        )
                .before(
                        new Date()
                );
    }

    // =====================================================
    // VALIDATE TOKEN
    // =====================================================

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ) {

        String email =
                extractEmail(
                        token
                );

        return email != null

                && email.equalsIgnoreCase(
                        userDetails.getUsername()
                )

                && !isTokenExpired(
                        token
                );
    }
}
