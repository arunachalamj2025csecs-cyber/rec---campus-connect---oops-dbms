package edu.rec.campusconnect.model;

/**
 * Notification and privacy switches chosen on the Settings page.
 * Plain Java object (no database annotations) so it can be embedded inside a User document.
 */
public class UserSettings {

    public boolean systemAlerts = true;
    public boolean complaintUpdates = true;
    public boolean eventInvitations = true;
    public boolean directoryVisibility = true;

    public UserSettings() {
    }
}
