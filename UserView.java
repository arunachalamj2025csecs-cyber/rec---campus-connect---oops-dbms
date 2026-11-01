package edu.rec.campusconnect.view;

import edu.rec.campusconnect.model.User;
import edu.rec.campusconnect.model.UserSettings;
import java.time.Instant;

/**
 * The public, safe version of a user that is sent to the browser.
 * It deliberately leaves out the password hash and the Google account ID.
 */
public record UserView(
        String id,
        String role,
        String name,
        String email,
        String phone,
        String department,
        String year,
        String rollNo,
        String employeeId,
        String profilePhotoUrl,
        UserSettings settings,
        String authProvider,
        String status,
        Instant createdAt) {

    public static UserView from(User user) {
        UserSettings settings = user.settings != null ? user.settings : new UserSettings();
        String provider = user.googleSub != null && !user.googleSub.isBlank() ? "google" : "password";
        return new UserView(
                user.id,
                user.role,
                user.name,
                user.email,
                user.phone,
                user.department,
                user.year,
                user.rollNo,
                user.employeeId,
                user.profilePhotoUrl,
                settings,
                provider,
                user.status,
                user.createdAt);
    }
}
