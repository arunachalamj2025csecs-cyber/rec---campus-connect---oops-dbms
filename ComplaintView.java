package edu.rec.campusconnect.view;

import edu.rec.campusconnect.model.Complaint;
import edu.rec.campusconnect.model.HistoryEntry;
import edu.rec.campusconnect.rules.ComplaintRules;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * The complaint as shown in the browser. adminContact is calculated, not stored:
 * it tells a student which administrator last handled the complaint.
 */
public record ComplaintView(
        String id,
        String studentId,
        String studentName,
        String title,
        String category,
        String description,
        String location,
        String priority,
        String status,
        List<HistoryEntry> history,
        Instant createdAt,
        Instant updatedAt,
        Map<String, Object> adminContact) {

    public static ComplaintView from(Complaint c) {
        List<HistoryEntry> history = c.history != null ? c.history : List.of();
        return new ComplaintView(
                c.id,
                c.studentId,
                c.studentName,
                c.title,
                c.category,
                c.description,
                c.location,
                c.priority,
                c.status,
                history,
                c.createdAt,
                c.updatedAt,
                ComplaintRules.adminContact(c.status, history));
    }
}
