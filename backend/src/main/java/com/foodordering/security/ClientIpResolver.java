package com.foodordering.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ClientIpResolver {

    // Validates standard IPv4 (e.g. 192.168.1.1)
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$"
    );

    // Validates standard IPv6 syntax or shorthand
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^[0-9a-fA-F:]+$"
    );

    /**
     * Resolves the true client IP address behind reverse proxies (Render, AWS ALB, Nginx, Cloudflare).
     * Validates IP syntax to prevent header injection or spoofing attacks.
     */
    public String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        // 1. Check Cloudflare header if present
        String cfConnectingIp = request.getHeader("CF-Connecting-IP");
        if (isValidIp(cfConnectingIp)) {
            return cfConnectingIp.trim();
        }

        // 2. Check X-Real-IP
        String realIp = request.getHeader("X-Real-IP");
        if (isValidIp(realIp)) {
            return realIp.trim();
        }

        // 3. Check X-Forwarded-For (can be a comma-separated list of hops: client, proxy1, proxy2)
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String[] parts = forwardedFor.split(",");
            for (String part : parts) {
                String candidate = part.trim();
                if (isValidIp(candidate) && !isTrustedInternalProxy(candidate)) {
                    return candidate;
                }
            }
            // If all are internal, return the first valid one
            String first = parts[0].trim();
            if (isValidIp(first)) {
                return first;
            }
        }

        // 4. Fallback to direct remote address
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr != null && !remoteAddr.isBlank()) ? remoteAddr.trim() : "127.0.0.1";
    }

    public boolean isValidIp(String ip) {
        if (ip == null || ip.isBlank() || ip.length() > 45) {
            return false;
        }
        String trimmed = ip.trim();
        return IPV4_PATTERN.matcher(trimmed).matches() || IPV6_PATTERN.matcher(trimmed).matches()
                || "localhost".equalsIgnoreCase(trimmed) || "::1".equals(trimmed);
    }

    public boolean isLocalhost(String ip) {
        if (ip == null) return false;
        String trimmed = ip.trim();
        return "127.0.0.1".equals(trimmed)
                || "0:0:0:0:0:0:0:1".equals(trimmed)
                || "::1".equals(trimmed)
                || "localhost".equalsIgnoreCase(trimmed);
    }

    private boolean isTrustedInternalProxy(String ip) {
        if (ip == null) return false;
        return ip.startsWith("10.") || ip.startsWith("192.168.") || ip.startsWith("172.16.")
                || ip.startsWith("172.17.") || ip.startsWith("172.18.") || ip.startsWith("172.19.")
                || ip.startsWith("172.2") || ip.startsWith("172.30.") || ip.startsWith("172.31.")
                || isLocalhost(ip);
    }
}

