package com.foodordering.common.util;

import com.foodordering.common.exception.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class ImageSanitizer {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Pattern SVG_SCRIPT_PATTERN = Pattern.compile("(?i)(<script|javascript:|onload|onerror|onclick|<foreignObject|<!ENTITY)");

    /**
     * Sanitizes a filename:
     * - Strips path traversal attempts (../, ..\, null bytes)
     * - Retains only safe alphanumeric characters, underscores, hyphens, and dots
     * - Validates extension against allowed image extensions
     * - Prepends a UUID prefix to avoid collisions and overwrite attacks
     */
    public String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return UUID.randomUUID() + ".jpg";
        }

        // Remove path traversal and null bytes
        String cleanName = originalFilename
                .replace("\\", "/")
                .replaceAll("\0", "");

        int lastSlash = cleanName.lastIndexOf('/');
        if (lastSlash >= 0) {
            cleanName = cleanName.substring(lastSlash + 1);
        }

        cleanName = cleanName.trim();

        // Extract extension
        int lastDot = cleanName.lastIndexOf('.');
        String ext = "";
        String base = cleanName;
        if (lastDot > 0 && lastDot < cleanName.length() - 1) {
            ext = cleanName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
            base = cleanName.substring(0, lastDot);
        }

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            ext = "jpg"; // Default safe extension
        }

        // Clean base name to alphanumeric characters only
        String safeBase = base.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (safeBase.isBlank()) {
            safeBase = "image";
        }

        if (safeBase.length() > 50) {
            safeBase = safeBase.substring(0, 50);
        }

        return UUID.randomUUID() + "_" + safeBase + "." + ext;
    }

    /**
     * Validates image content from byte array using magic numbers (file signatures).
     * Rejects corrupted, disguised executable, or script payloads.
     */
    public boolean isValidImageBytes(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return false;
        }

        // JPEG: FF D8 FF
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return true;
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((bytes[0] & 0xFF) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47
                && bytes[4] == 0x0D && bytes[5] == 0x0A && bytes[6] == 0x1A && bytes[7] == 0x0A) {
            return true;
        }

        // GIF: GIF87a or GIF89a
        if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8'
                && (bytes[4] == '7' || bytes[4] == '9') && bytes[5] == 'a') {
            return true;
        }

        // WebP: RIFF .... WEBP
        if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') {
            return true;
        }

        return false;
    }

    /**
     * Validates an image URL or Base64 data string before saving.
     * Throws BusinessRuleException if invalid or dangerous.
     */
    public String validateAndSanitizeImage(String imageInput) {
        if (imageInput == null || imageInput.isBlank()) {
            return null;
        }

        String trimmed = imageInput.trim();

        // 1. Check if it is a Base64 data URI (e.g. data:image/png;base64,....)
        if (trimmed.startsWith("data:image/")) {
            int commaIndex = trimmed.indexOf(',');
            if (commaIndex <= 0) {
                throw new BusinessRuleException("Malformed base64 image data");
            }

            String header = trimmed.substring(0, commaIndex).toLowerCase(Locale.ROOT);
            if (!header.contains(";base64")) {
                throw new BusinessRuleException("Only base64 encoded images are supported");
            }

            String mime = header.replace("data:", "").split(";")[0].trim();
            boolean allowedMime = ALLOWED_MIME_TYPES.contains(mime);
            if (!allowedMime) {
                throw new BusinessRuleException("Unsupported image format. Allowed formats: JPEG, PNG, WebP, GIF");
            }

            String base64Data = trimmed.substring(commaIndex + 1);
            byte[] decoded;
            try {
                decoded = Base64.getDecoder().decode(base64Data);
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("Invalid base64 image content");
            }

            // Size limit: 5MB
            if (decoded.length > 5 * 1024 * 1024) {
                throw new BusinessRuleException("Uploaded image exceeds 5MB size limit");
            }

            if (!isValidImageBytes(decoded)) {
                throw new BusinessRuleException("Uploaded file does not match a valid image signature");
            }

            return trimmed;
        }

        // 2. Check if it is a standard HTTP / HTTPS URL
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            if (trimmed.length() > 2000) {
                throw new BusinessRuleException("Image URL is too long (maximum 2000 characters)");
            }

            // Prevent XSS / script inclusion in URL strings
            if (SVG_SCRIPT_PATTERN.matcher(trimmed).find()) {
                throw new BusinessRuleException("Image URL contains invalid or disallowed characters");
            }

            return trimmed;
        }

        throw new BusinessRuleException("Image must be a valid HTTP/HTTPS URL or Base64 image");
    }
}

