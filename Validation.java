package edu.rec.campusconnect.common;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Input checks shared by all services.
 *
 * Each method either returns a cleaned value or throws an ApiException (HTTP 400)
 * with a message that a student or administrator can understand.
 * Keeping every rule in one place makes the rules easy to explain and to test.
 */
public final class Validation {

    /** A student email: anything before @rajalakshmi.edu.in (no spaces, only one @). */
    private static final Pattern STUDENT_EMAIL = Pattern.compile("^[^\\s@]+@rajalakshmi\\.edu\\.in$");
    private static final Pattern ISO_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    public static final List<String> YEARS = List.of("1st Year", "2nd Year", "3rd Year", "4th Year");
    public static final List<String> COMPLAINT_CATEGORIES = List.of(
            "Infrastructure", "Academics", "Maintenance", "Hostel", "Transport", "Library", "IT / Wi-Fi", "Other");
    public static final List<String> PRIORITIES = List.of("Low", "Medium", "High", "Critical");
    public static final List<String> COMPLAINT_STATUSES = List.of("open", "inprogress", "resolved");
    public static final List<String> EVENT_CATEGORIES = List.of(
            "Academic", "Placement", "Workshop", "Cultural", "Sports", "General");
    public static final List<String> EVENT_STATUSES = List.of("draft", "published");

    private Validation() {
    }

    /** Lower-cases and trims an email address. Returns "" for null. */
    public static String normalizeEmail(String value) {
        return trimmed(value).toLowerCase(Locale.ROOT);
    }

    /** True when the (already normalized) email ends with @rajalakshmi.edu.in. */
    public static boolean isStudentEmail(String normalizedEmail) {
        return STUDENT_EMAIL.matcher(normalizedEmail).matches();
    }

    /** Required text: trimmed, must not be empty, must not be longer than maxLength. */
    public static String requireText(String value, String label, int maxLength) {
        String cleaned = trimmed(value);
        if (cleaned.isEmpty()) {
            throw ApiException.badRequest(label + " is required.");
        }
        checkLength(cleaned, label, maxLength);
        return cleaned;
    }

    /** Optional text: null becomes "", otherwise trimmed and length-checked. */
    public static String optionalText(String value, String label, int maxLength) {
        String cleaned = trimmed(value);
        checkLength(cleaned, label, maxLength);
        return cleaned;
    }

    /** Required value that must be one of the allowed list. */
    public static String requireOneOf(String value, String label, List<String> allowed) {
        String cleaned = trimmed(value);
        if (!allowed.contains(cleaned)) {
            throw ApiException.badRequest(label + " must be one of: " + String.join(", ", allowed) + ".");
        }
        return cleaned;
    }

    /** Optional value: blank means "use the fallback"; otherwise it must be in the allowed list. */
    public static String oneOfOrDefault(String value, String label, List<String> allowed, String fallback) {
        String cleaned = trimmed(value);
        if (cleaned.isEmpty()) {
            return fallback;
        }
        return requireOneOf(cleaned, label, allowed);
    }

    /** Passwords are NOT trimmed (spaces can be part of a password). Minimum 6 characters. */
    public static String requirePassword(String password) {
        if (password == null || password.length() < 6) {
            throw ApiException.badRequest("Password must be at least 6 characters.");
        }
        if (password.length() > 200) {
            throw ApiException.badRequest("Password must be 200 characters or fewer.");
        }
        return password;
    }

    /** True when the text is a real calendar date written as YYYY-MM-DD. */
    public static boolean isIsoDate(String value) {
        if (value == null || !ISO_DATE.matcher(value).matches()) {
            return false;
        }
        try {
            LocalDate.parse(value);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    public static String requireDate(String value) {
        if (!isIsoDate(value)) {
            throw ApiException.badRequest("Event date must be a valid date in YYYY-MM-DD format.");
        }
        return value;
    }

    /** Converts null to "" and trims everything else. */
    public static String trimmed(String value) {
        return value == null ? "" : value.trim();
    }

    private static void checkLength(String value, String label, int maxLength) {
        if (value.length() > maxLength) {
            throw ApiException.badRequest(label + " must be " + maxLength + " characters or fewer.");
        }
    }
}
