package edu.rec.campusconnect.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/** A complaint raised by a student, stored in the "complaints" collection. */
@Document(collection = "complaints")
public class Complaint {

    @Id
    public String id;

    @Indexed
    public String studentId;

    public String studentName;
    public String title;
    public String category;
    public String description;
    public String location;
    public String priority;

    /** "open", "inprogress" or "resolved". */
    public String status;

    public List<HistoryEntry> history = new ArrayList<>();

    public Instant createdAt;
    public Instant updatedAt;

    public Complaint() {
    }
}
