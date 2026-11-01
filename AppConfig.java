package edu.rec.campusconnect.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Settings read from environment variables (see application.properties).
 * Keeping secrets out of the code means they can be changed without editing source files.
 */
@Component
public class AppConfig {

    private final String googleClientId;
    private final Set<String> adminEmails;
    private final String adminRegistrationKey;
    private final List<String> allowedOrigins;
    private final long sessionHours;
    private final String timezone;

    public AppConfig(
            @Value("${app.google-client-id:}") String googleClientId,
            @Value("${app.admin-emails:}") String adminEmails,
            @Value("${app.admin-registration-key:}") String adminRegistrationKey,
            @Value("${app.cors-allowed-origins:}") String allowedOrigins,
            @Value("${app.session-hours:6}") long sessionHours,
            @Value("${app.timezone:Asia/Kolkata}") String timezone) {
        this.googleClientId = googleClientId == null ? "" : googleClientId.trim();
        this.adminEmails = parseList(adminEmails).stream()
                .map(e -> e.toLowerCase(Locale.ROOT))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        this.adminRegistrationKey = adminRegistrationKey == null ? "" : adminRegistrationKey.trim();
        this.allowedOrigins = parseList(allowedOrigins);
        this.sessionHours = sessionHours;
        this.timezone = timezone;
    }

    public String googleClientId() {
        return googleClientId;
    }

    public boolean isAdminEmail(String normalizedEmail) {
        return adminEmails.contains(normalizedEmail);
    }

    public Set<String> adminEmails() {
        return Collections.unmodifiableSet(adminEmails);
    }

    public String adminRegistrationKey() {
        return adminRegistrationKey;
    }

    public List<String> allowedOrigins() {
        return allowedOrigins;
    }

    public long sessionHours() {
        return sessionHours;
    }

    public String timezone() {
        return timezone;
    }

    /** Splits "a@x.com, b@y.com" into a clean list. Empty items are dropped. */
    static List<String> parseList(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
