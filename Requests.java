package edu.rec.campusconnect.dto;

/**
 * The JSON bodies the frontend sends. Each record is one request shape.
 * Missing fields simply arrive as null, and the services validate them.
 */
public final class Requests {

    private Requests() {
    }

    public record RegisterStudent(String name, String email, String phone, String rollNo,
                                  String department, String year, String password) {
    }

    public record RegisterAdmin(String name, String email, String phone, String department,
                                String password, String adminKey) {
    }

    public record Login(String email, String password, String role) {
    }

    public record GoogleLogin(String credential, String role) {
    }

    public record ChangePassword(String currentPassword, String newPassword) {
    }

    /** identifier = roll number for students, employee ID for admins. */
    public record ProfileUpdate(String name, String phone, String department, String identifier,
                                String year, Upload profilePhoto) {
    }

    public record ProfileRequest(ProfileUpdate profile) {
    }

    /** Null means "leave this switch as it is". */
    public record Settings(Boolean systemAlerts, Boolean complaintUpdates,
                           Boolean eventInvitations, Boolean directoryVisibility) {
    }

    public record SettingsRequest(Settings settings) {
    }

    /** An image sent as base64 text. Used for event posters and profile photos. */
    public record Upload(String name, String type, String data) {
    }

    public record ComplaintInput(String title, String category, String description,
                                 String location, String priority) {
    }

    public record CreateComplaint(ComplaintInput complaint) {
    }

    public record UpdateComplaint(String status, String note) {
    }

    public record EventInput(String id, String title, String description, String date, String time,
                             String location, String category, String status, Upload poster) {
    }

    public record EventRequest(EventInput event) {
    }
}
