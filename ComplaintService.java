package edu.rec.campusconnect.service;

import edu.rec.campusconnect.common.ApiException;
import edu.rec.campusconnect.common.Validation;
import edu.rec.campusconnect.dto.Requests;
import edu.rec.campusconnect.model.Complaint;
import edu.rec.campusconnect.model.HistoryEntry;
import edu.rec.campusconnect.model.User;
import edu.rec.campusconnect.repository.ComplaintRepository;
import edu.rec.campusconnect.rules.ComplaintRules;
import edu.rec.campusconnect.security.Tokens;
import edu.rec.campusconnect.view.ComplaintView;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

/**
 * Everything about complaints: filing, viewing, status changes, search and summary counts.
 * Students only ever see their own complaints; administrators see all of them.
 */
@Service
public class ComplaintService {

    private static final Comparator<Complaint> NEWEST_FIRST = Comparator.comparing(
            (Complaint c) -> c.createdAt, Comparator.nullsLast(Comparator.<Instant>reverseOrder()));

    private final ComplaintRepository complaints;

    public ComplaintService(ComplaintRepository complaints) {
        this.complaints = complaints;
    }

    /** Lists complaints, newest first. status = all | open | inprogress | resolved. */
    public List<ComplaintView> list(User viewer, String status) {
        String filter = Validation.trimmed(status).isEmpty() ? "all" : status.trim();
        if (!"all".equals(filter) && !Validation.COMPLAINT_STATUSES.contains(filter)) {
            throw ApiException.badRequest("Status filter must be all, open, inprogress or resolved.");
        }
        List<Complaint> visible = visibleTo(viewer);
        return visible.stream()
                .filter(c -> "all".equals(filter) || filter.equals(c.status))
                .sorted(NEWEST_FIRST)
                .map(ComplaintView::from)
                .toList();
    }

    /** Case-insensitive search over ID, title, category and student name. */
    public List<ComplaintView> search(User viewer, String query) {
        String q = Validation.trimmed(query).toLowerCase(Locale.ROOT);
        return list(viewer, "all").stream()
                .filter(c -> q.isEmpty() || (c.id() + " " + c.title() + " " + c.category() + " "
                        + c.studentName()).toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }

    /** Counts for the admin dashboard: totals per status and per category. */
    public Map<String, Object> summary() {
        List<Complaint> all = complaints.findAll();
        Map<String, Integer> byCategory = new LinkedHashMap<>();
        for (String category : Validation.COMPLAINT_CATEGORIES) {
            byCategory.put(category, 0);
        }
        for (Complaint c : all) {
            String category = c.category == null ? "Other" : c.category;
            byCategory.merge(category, 1, Integer::sum);
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("total", all.size());
        summary.put("open", countWithStatus(all, "open"));
        summary.put("inProgress", countWithStatus(all, "inprogress"));
        summary.put("resolved", countWithStatus(all, "resolved"));
        summary.put("byCategory", byCategory);
        return summary;
    }

    /** A student files a complaint. */
    public ComplaintView create(User student, Requests.ComplaintInput d) {
        if (d == null) {
            throw ApiException.badRequest("Complaint details are required.");
        }
        String title = Validation.requireText(d.title(), "Complaint title", 150);
        String description = Validation.requireText(d.description(), "Description", 3000);
        String category = Validation.oneOfOrDefault(d.category(), "Category", Validation.COMPLAINT_CATEGORIES, "Other");
        String priority = Validation.oneOfOrDefault(d.priority(), "Priority", Validation.PRIORITIES, "Medium");
        String location = Validation.optionalText(d.location(), "Location", 150);

        Instant now = Instant.now();
        HistoryEntry first = new HistoryEntry();
        first.type = "system";
        first.action = "Complaint submitted";
        first.note = "Complaint received by REC CampusConnect.";
        first.at = now;
        first.by = student.name;

        Complaint c = new Complaint();
        c.id = Tokens.readableId("CMP");
        c.studentId = student.id;
        c.studentName = student.name;
        c.title = title;
        c.category = category;
        c.description = description;
        c.location = location;
        c.priority = priority;
        c.status = "open";
        c.history = new ArrayList<>(List.of(first));
        c.createdAt = now;
        c.updatedAt = now;
        return ComplaintView.from(complaints.save(c));
    }

    /** Opens one complaint. An administrator opening it is recorded in the activity log. */
    public ComplaintView get(User viewer, String id) {
        Complaint c = find(id);
        boolean admin = AuthService.ADMIN.equals(viewer.role);
        if (!admin && !Objects.equals(c.studentId, viewer.id)) {
            throw ApiException.forbidden("You are not allowed to view this complaint.");
        }
        if (admin) {
            c = recordAdminView(c, viewer);
        }
        return ComplaintView.from(c);
    }

    /** An administrator changes the status and optionally adds a note. */
    public ComplaintView updateStatus(User admin, String id, Requests.UpdateComplaint r) {
        if (r == null) {
            throw ApiException.badRequest("Status update details are required.");
        }
        String status = Validation.requireOneOf(r.status(), "Status", Validation.COMPLAINT_STATUSES);
        String note = Validation.optionalText(r.note(), "Admin note", 1000);

        Complaint c = recordAdminView(find(id), admin);
        Instant now = Instant.now();
        HistoryEntry entry = adminEntry("status", "Status changed to " + ComplaintRules.statusLabel(status),
                note.isEmpty() ? "No additional note." : note, now, admin);
        entry.status = status;
        c.history.add(entry);
        c.status = status;
        c.updatedAt = now;
        return ComplaintView.from(complaints.save(c));
    }

    // ---------------------------------------------------------------- helpers

    private List<Complaint> visibleTo(User viewer) {
        if (AuthService.ADMIN.equals(viewer.role)) {
            return complaints.findAll();
        }
        return complaints.findByStudentId(viewer.id);
    }

    private Complaint find(String id) {
        Complaint c = complaints.findById(id)
                .orElseThrow(() -> ApiException.notFound("Complaint not found."));
        if (c.history == null) {
            c.history = new ArrayList<>();
        }
        return c;
    }

    private Complaint recordAdminView(Complaint c, User admin) {
        Instant now = Instant.now();
        if (!ComplaintRules.shouldRecordAdminView(c.history, admin.id, now)) {
            return c;
        }
        c.history.add(adminEntry("admin_view", "Viewed by administrator",
                "An administrator opened this complaint.", now, admin));
        c.updatedAt = now;
        return complaints.save(c);
    }

    private static HistoryEntry adminEntry(String type, String action, String note, Instant now, User admin) {
        HistoryEntry entry = new HistoryEntry();
        entry.type = type;
        entry.action = action;
        entry.note = note;
        entry.at = now;
        entry.by = admin.name;
        entry.adminId = admin.id;
        entry.adminName = admin.name;
        entry.adminEmail = admin.email;
        entry.adminPhone = admin.phone == null ? "" : admin.phone;
        return entry;
    }

    private static int countWithStatus(List<Complaint> all, String status) {
        return (int) all.stream().filter(c -> status.equals(c.status)).count();
    }
}
