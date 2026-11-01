package edu.rec.campusconnect.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class FileRulesTest {

    @Test
    void imageTypesAreNormalized() {
        assertEquals("image/jpeg", FileRules.normalizeImageType("image/jpg"));
        assertEquals("image/png", FileRules.normalizeImageType("IMAGE/PNG"));
        assertNull(FileRules.normalizeImageType("text/html"));
        assertNull(FileRules.normalizeImageType(null));
    }

    @Test
    void magicBytesMustMatchTheDeclaredType() {
        byte[] png = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0};
        byte[] webp = "RIFF\0\0\0\0WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1);
        byte[] html = "<html>".getBytes(StandardCharsets.UTF_8);
        assertTrue(FileRules.matchesSignature("image/png", png));
        assertTrue(FileRules.matchesSignature("image/jpeg", jpeg));
        assertTrue(FileRules.matchesSignature("image/webp", webp));
        assertFalse(FileRules.matchesSignature("image/png", html));
        assertFalse(FileRules.matchesSignature("image/jpeg", png));
    }
}
