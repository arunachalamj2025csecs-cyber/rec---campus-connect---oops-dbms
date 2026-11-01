package edu.rec.campusconnect.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ValidationTest {

    @Test
    void normalizeEmailTrimsAndLowercases() {
        assertEquals("student@rajalakshmi.edu.in", Validation.normalizeEmail("  Student@RajalakshmI.edu.in "));
        assertEquals("", Validation.normalizeEmail(null));
    }

    @Test
    void studentEmailMustEndWithCollegeDomain() {
        assertTrue(Validation.isStudentEmail("first.last@rajalakshmi.edu.in"));
        assertTrue(Validation.isStudentEmail("arunachalam.j.2025.csecs@rajalakshmi.edu.in"));
        assertFalse(Validation.isStudentEmail("someone@gmail.com"));
        assertFalse(Validation.isStudentEmail("someone@x.rajalakshmi.edu.in"));
        assertFalse(Validation.isStudentEmail("@rajalakshmi.edu.in"));
        assertFalse(Validation.isStudentEmail("two words@rajalakshmi.edu.in"));
    }

    @Test
    void requireTextRejectsBlankAndLongValues() {
        assertEquals("Hello", Validation.requireText("  Hello  ", "Name", 10));
        assertThrows(ApiException.class, () -> Validation.requireText("   ", "Name", 10));
        assertThrows(ApiException.class, () -> Validation.requireText("12345678901", "Name", 10));
    }

    @Test
    void passwordNeedsSixCharacters() {
        assertThrows(ApiException.class, () -> Validation.requirePassword("12345"));
        assertEquals("123456", Validation.requirePassword("123456"));
    }

    @Test
    void isoDatesMustBeRealDates() {
        assertTrue(Validation.isIsoDate("2026-02-28"));
        assertFalse(Validation.isIsoDate("2026-02-30"));
        assertFalse(Validation.isIsoDate("28-02-2026"));
        assertFalse(Validation.isIsoDate(null));
    }

    @Test
    void oneOfOrDefaultUsesFallbackWhenBlank() {
        List<String> allowed = List.of("Low", "High");
        assertEquals("Low", Validation.oneOfOrDefault("  ", "Priority", allowed, "Low"));
        assertEquals("High", Validation.oneOfOrDefault("High", "Priority", allowed, "Low"));
        assertThrows(ApiException.class, () -> Validation.oneOfOrDefault("Urgent", "Priority", allowed, "Low"));
    }
}
