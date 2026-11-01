package edu.rec.campusconnect.service;

import edu.rec.campusconnect.common.ApiException;
import edu.rec.campusconnect.common.Validation;
import edu.rec.campusconnect.config.AppConfig;
import edu.rec.campusconnect.dto.Requests;
import edu.rec.campusconnect.model.Event;
import edu.rec.campusconnect.model.StoredFile;
import edu.rec.campusconnect.model.User;
import edu.rec.campusconnect.repository.EventRepository;
import edu.rec.campusconnect.rules.EventRules;
import edu.rec.campusconnect.security.Tokens;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Service;

/** Campus events: create, edit, publish, delete and the events board. */
@Service
public class EventService {

    private final EventRepository events;
    private final FileService files;
    private final AppConfig config;

    public EventService(EventRepository events, FileService files, AppConfig config) {
        this.events = events;
        this.files = files;
        this.config = config;
    }

    /** Today's date in the campus time zone (Asia/Kolkata by default). */
    public LocalDate today() {
        return LocalDate.now(ZoneId.of(config.timezone()));
    }

    /** Published events sorted for the board: upcoming first, then past. */
    public List<Event> publishedSorted() {
        LocalDate today = today();
        return events.findAll().stream()
                .filter(e -> "published".equals(e.status))
                .sorted((a, b) -> EventRules.boardOrder(a.date, b.date, today))
                .toList();
    }

    /** Students see published events only. Administrators see drafts too. */
    public List<Event> list(User viewer) {
        if (AuthService.ADMIN.equals(viewer.role)) {
            LocalDate today = today();
            return events.findAll().stream()
                    .sorted((a, b) -> EventRules.boardOrder(a.date, b.date, today))
                    .toList();
        }
        return publishedSorted();
    }

    public Event get(User viewer, String id) {
        Event event = find(id);
        boolean admin = AuthService.ADMIN.equals(viewer.role);
        if (!admin && !"published".equals(event.status)) {
            throw ApiException.notFound("Event not found.");
        }
        return event;
    }

    public Event create(User admin, Requests.EventInput d) {
        if (d == null) {
            throw ApiException.badRequest("Event details are required.");
        }
        Instant now = Instant.now();
        Event event = new Event();
        event.id = Tokens.readableId("EVT");
        event.title = Validation.requireText(d.title(), "Event title", 150);
        event.description = Validation.requireText(d.description(), "Description", 3000);
        event.date = Validation.requireDate(Validation.trimmed(d.date()));
        event.time = Validation.optionalText(d.time(), "Time", 50);
        event.location = Validation.optionalText(d.location(), "Location", 150);
        event.category = Validation.oneOfOrDefault(d.category(), "Category", Validation.EVENT_CATEGORIES, "General");
        event.status = Validation.oneOfOrDefault(d.status(), "Status", Validation.EVENT_STATUSES, "draft");
        event.createdBy = admin.id;
        event.createdAt = now;
        event.updatedAt = now;

        if (d.poster() != null && d.poster().data() != null && !d.poster().data().isBlank()) {
            StoredFile poster = files.saveImage(admin.id, d.poster(), FileService.MAX_POSTER_BYTES);
            event.posterFileId = poster.id;
            event.posterUrl = FileService.urlFor(poster.id);
        }
        return events.save(event);
    }

    /** Partial update: only the fields that were sent change. An empty string clears an optional field. */
    public Event update(User admin, String id, Requests.EventInput d) {
        if (d == null) {
            throw ApiException.badRequest("Event details are required.");
        }
        Event event = find(id);
        if (d.title() != null) {
            event.title = Validation.requireText(d.title(), "Event title", 150);
        }
        if (d.description() != null) {
            event.description = Validation.requireText(d.description(), "Description", 3000);
        }
        if (d.date() != null) {
            event.date = Validation.requireDate(Validation.trimmed(d.date()));
        }
        if (d.time() != null) {
            event.time = Validation.optionalText(d.time(), "Time", 50);
        }
        if (d.location() != null) {
            event.location = Validation.optionalText(d.location(), "Location", 150);
        }
        if (d.category() != null) {
            event.category = Validation.oneOfOrDefault(d.category(), "Category", Validation.EVENT_CATEGORIES, "General");
        }
        if (d.status() != null) {
            event.status = Validation.requireOneOf(d.status(), "Status", Validation.EVENT_STATUSES);
        }
        if (d.poster() != null && d.poster().data() != null && !d.poster().data().isBlank()) {
            String oldPosterId = event.posterFileId;
            StoredFile poster = files.saveImage(admin.id, d.poster(), FileService.MAX_POSTER_BYTES);
            event.posterFileId = poster.id;
            event.posterUrl = FileService.urlFor(poster.id);
            files.deleteQuietly(oldPosterId);
        }
        event.updatedAt = Instant.now();
        return events.save(event);
    }

    public void delete(String id) {
        Event event = find(id);
        files.deleteQuietly(event.posterFileId);
        events.deleteById(event.id);
    }

    private Event find(String id) {
        return events.findById(id).orElseThrow(() -> ApiException.notFound("Event not found."));
    }
}
