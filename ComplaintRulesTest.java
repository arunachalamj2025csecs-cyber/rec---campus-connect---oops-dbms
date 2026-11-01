package edu.rec.campusconnect.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.rec.campusconnect.model.HistoryEntry;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ComplaintRulesTest {

    private static HistoryEntry view(String adminId, Instant at) {
        HistoryEntry entry = new HistoryEntry();
        entry.type = "admin_view";
        entry.adminId = adminId;
        entry.at = at;
        return entry;
    }

    private static HistoryEntry statusChange(String status, String adminName, String adminEmail) {
        HistoryEntry entry = new HistoryEntry();
        entry.type = "status";
        entry.status = status;
        entry.adminName = adminName;
        entry.adminEmail = adminEmail;
        entry.action = "Status changed";
        entry.at = Instant.parse("2026-01-02T10:00:00Z");
        return entry;
    }

    @Test
    void statusLabelsAreReadable() {
        assertEquals("In Progress", ComplaintRules.statusLabel("inprogress"));
        assertEquals("Resolved", ComplaintRules.statusLabel("resolved"));
        assertEquals("Open", ComplaintRules.statusLabel("open"));
    }

    @Test
    void firstAdminViewIsRecorded() {
        Instant now = Instant.parse("2026-01-02T10:00:00Z");
        assertTrue(ComplaintRules.shouldRecordAdminView(new ArrayList<>(), "ADM-1", now));
    }

    @Test
    void repeatedViewWithinAMinuteIsNotRecorded() {
        Instant start = Instant.parse("2026-01-02T10:00:00Z");
        List<HistoryEntry> history = new ArrayList<>();
        history.add(view("ADM-1", start));
        assertFalse(ComplaintRules.shouldRecordAdminView(history, "ADM-1", start.plusSeconds(30)));
        assertTrue(ComplaintRules.shouldRecordAdminView(history, "ADM-1", start.plusSeconds(61)));
        assertTrue(ComplaintRules.shouldRecordAdminView(history, "ADM-2", start.plusSeconds(30)));
    }

    @Test
    void adminContactIsNullWhenNobodyHasActed() {
        assertNull(ComplaintRules.adminContact("open", new ArrayList<>()));
    }

    @Test
    void adminContactPrefersLatestStatusChangeWithCurrentStatus() {
        List<HistoryEntry> history = new ArrayList<>();
        history.add(statusChange("inprogress", "Old Admin", "old@rajalakshmi.edu.in"));
        history.add(statusChange("resolved", "Newest Admin", "new@rajalakshmi.edu.in"));
        history.add(statusChange("inprogress", "Current Admin", "cur@rajalakshmi.edu.in"));
        Map<String, Object> contact = ComplaintRules.adminContact("inprogress", history);
        assertEquals("Current Admin", contact.get("name"));
        assertEquals("cur@rajalakshmi.edu.in", contact.get("email"));
    }

    @Test
    void adminContactFallsBackToViewWhenThereIsNoStatusChange() {
        List<HistoryEntry> history = new ArrayList<>();
        HistoryEntry viewer = view("ADM-9", Instant.parse("2026-01-02T10:00:00Z"));
        viewer.adminName = "Viewer Admin";
        history.add(viewer);
        Map<String, Object> contact = ComplaintRules.adminContact("open", history);
        assertEquals("Viewer Admin", contact.get("name"));
        assertEquals("Viewed by administrator", contact.get("action"));
    }
}
