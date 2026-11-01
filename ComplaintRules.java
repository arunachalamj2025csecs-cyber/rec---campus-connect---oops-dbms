package edu.rec.campusconnect.rules;

import edu.rec.campusconnect.model.HistoryEntry;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure rules for complaints (no database, no web code), so they are easy to read and test.
 */
public final class ComplaintRules {

    /** An admin opening the same complaint again within this time is not logged a second time. */
    public static final Duration VIEW_LOG_INTERVAL = Duration.ofSeconds(60);

    private ComplaintRules() {
    }

    /** Human-readable status shown to users. */
    public static String statusLabel(String status) {
        if ("inprogress".equals(status)) {
            return "In Progress";
        }
        if ("resolved".equals(status)) {
            return "Resolved";
        }
        return "Open";
    }

    /** Should a new "admin_view" line be added to the activity log? */
    public static boolean shouldRecordAdminView(List<HistoryEntry> history, String adminId, Instant now) {
        HistoryEntry last = null;
        for (HistoryEntry entry : history) {
            if ("admin_view".equals(entry.type)) {
                last = entry;
            }
        }
        if (last == null || !adminId.equals(last.adminId) || last.at == null) {
            return true;
        }
        return Duration.between(last.at, now).compareTo(VIEW_LOG_INTERVAL) > 0;
    }

    /**
     * Works out which administrator a student should contact about this complaint.
     *
     * Preference order:
     *  1. the latest status change that matches the current status,
     *  2. the latest status change of any kind,
     *  3. the latest time an administrator opened the complaint.
     *
     * Returns null when no administrator has touched the complaint yet.
     */
    public static Map<String, Object> adminContact(String currentStatus, List<HistoryEntry> history) {
        HistoryEntry lastStatus = null;
        HistoryEntry lastMatchingStatus = null;
        HistoryEntry lastView = null;
        for (HistoryEntry entry : history) {
            if ("status".equals(entry.type)) {
                lastStatus = entry;
                if (entry.status == null || entry.status.equals(currentStatus)) {
                    lastMatchingStatus = entry;
                }
            } else if ("admin_view".equals(entry.type)) {
                lastView = entry;
            }
        }
        HistoryEntry chosen = lastMatchingStatus != null ? lastMatchingStatus : lastStatus;
        if (chosen == null) {
            chosen = lastView;
        }
        if (chosen == null) {
            return null;
        }
        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("id", nonNull(chosen.adminId));
        contact.put("name", firstNonBlank(chosen.adminName, chosen.by, "Administrator"));
        contact.put("email", nonNull(chosen.adminEmail));
        contact.put("phone", nonNull(chosen.adminPhone));
        contact.put("seenAt", chosen.at);
        contact.put("action", firstNonBlank(chosen.action, "Viewed by administrator"));
        contact.put("status", firstNonBlank(chosen.status, currentStatus));
        return contact;
    }

    private static String nonNull(String value) {
        return value == null ? "" : value;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "";
    }
}
