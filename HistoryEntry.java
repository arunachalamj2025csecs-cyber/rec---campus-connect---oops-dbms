package edu.rec.campusconnect.model;

import java.time.Instant;

/**
 * One line in a complaint's activity log.
 *
 * type = "system"     -> complaint was submitted
 * type = "admin_view" -> an administrator opened the complaint (recorded at most once a minute per admin)
 * type = "status"     -> an administrator changed the status and may have added a note
 */
public class HistoryEntry {

    public String type;
    public String action;
    public String status;
    public String note;
    public Instant at;
    public String by;
    public String adminId;
    public String adminName;
    public String adminEmail;
    public String adminPhone;

    public HistoryEntry() {
    }
}
