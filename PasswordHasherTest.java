package edu.rec.campusconnect.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void correctPasswordMatches() {
        String stored = PasswordHasher.hash("secret123");
        assertTrue(PasswordHasher.matches("secret123", stored));
    }

    @Test
    void wrongPasswordDoesNotMatch() {
        String stored = PasswordHasher.hash("secret123");
        assertFalse(PasswordHasher.matches("secret124", stored));
    }

    @Test
    void samepasswordGivesDifferentHashesBecauseOfSalt() {
        assertNotEquals(PasswordHasher.hash("secret123"), PasswordHasher.hash("secret123"));
    }

    @Test
    void malformedStoredValuesNeverMatch() {
        assertFalse(PasswordHasher.matches("secret123", null));
        assertFalse(PasswordHasher.matches("secret123", ""));
        assertFalse(PasswordHasher.matches("secret123", "plain-text-password"));
        assertFalse(PasswordHasher.matches("secret123", "pbkdf2_sha256$abc$!!$!!"));
    }
}
