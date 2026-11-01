package edu.rec.campusconnect.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** A campus event or notice, stored in the "events" collection. */
@Document(collection = "events")
public class Event {

    @Id
    public String id;

    public String title;
    public String description;

    /** Date as text, YYYY-MM-DD. */
    public String date;

    public String time;
    public String location;
    public String category;

    /** Link to the poster image (relative, e.g. /api/files/abc), or null. */
    public String posterUrl;
    public String posterFileId;

    /** "draft" (only admins see it) or "published" (everyone sees it). */
    public String status;

    public String createdBy;
    public Instant createdAt;
    public Instant updatedAt;

    public Event() {
    }
}
