package com.foodordering.common.util;

import com.foodordering.common.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ImageSanitizerTest {

    private ImageSanitizer imageSanitizer;

    @BeforeEach
    void setUp() {
        imageSanitizer = new ImageSanitizer();
    }

    @Test
    void testSanitizeFilename_PreventsPathTraversalAndSpecialChars() {
        String dangerousFilename = "../../../etc/passwd/hack.exe";
        String sanitized = imageSanitizer.sanitizeFilename(dangerousFilename);

        assertNotNull(sanitized);
        assertFalse(sanitized.contains(".."));
        assertFalse(sanitized.contains("/"));
        assertFalse(sanitized.contains("\\"));
        assertTrue(sanitized.endsWith(".jpg")); // Non-image extension defaulted to safe .jpg
    }

    @Test
    void testSanitizeFilename_ValidImageName_KeepsSafeBaseAndExtension() {
        String original = "delicious_burger.png";
        String sanitized = imageSanitizer.sanitizeFilename(original);

        assertNotNull(sanitized);
        assertTrue(sanitized.contains("delicious_burger"));
        assertTrue(sanitized.endsWith(".png"));
    }

    @Test
    void testValidateAndSanitizeImage_ValidHttpUrl_Allowed() {
        String validUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c";
        String result = imageSanitizer.validateAndSanitizeImage(validUrl);

        assertEquals(validUrl, result);
    }

    @Test
    void testValidateAndSanitizeImage_ScriptInUrl_Rejected() {
        String scriptUrl = "https://example.com/img.png?<script>alert(1)</script>";

        assertThrows(BusinessRuleException.class, () ->
                imageSanitizer.validateAndSanitizeImage(scriptUrl)
        );
    }

    @Test
    void testValidateAndSanitizeImage_ValidPngBase64_Allowed() {
        // Valid PNG header: 89 50 4E 47 0D 0A 1A 0A followed by dummy bytes
        byte[] pngHeader = new byte[] {
                (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47,
                (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52
        };
        String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(pngHeader);

        String result = imageSanitizer.validateAndSanitizeImage(base64);
        assertNotNull(result);
        assertTrue(result.startsWith("data:image/png;base64,"));
    }

    @Test
    void testValidateAndSanitizeImage_InvalidMagicBytes_Rejected() {
        // Random text payload pretending to be a PNG
        byte[] textPayload = "HELLO_NOT_AN_IMAGE_FILE".getBytes();
        String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(textPayload);

        assertThrows(BusinessRuleException.class, () ->
                imageSanitizer.validateAndSanitizeImage(base64)
        );
    }
}

