package edu.rec.campusconnect.service;

import edu.rec.campusconnect.common.ApiException;
import edu.rec.campusconnect.common.Validation;
import edu.rec.campusconnect.dto.Requests;
import edu.rec.campusconnect.model.User;
import edu.rec.campusconnect.model.UserSettings;
import edu.rec.campusconnect.repository.UserRepository;
import edu.rec.campusconnect.repository.UserSessionRepository;
import edu.rec.campusconnect.security.PasswordHasher;
import java.time.Instant;
import java.util.Locale;
import org.springframework.stereotype.Service;

/** Profile, notification settings and password changes for the logged-in user. */
@Service
public class UserService {

    private final UserRepository users;
    private final UserSessionRepository sessions;
    private final FileService files;

    public UserService(UserRepository users, UserSessionRepository sessions, FileService files) {
        this.users = users;
        this.sessions = sessions;
        this.files = files;
    }

    /** Updates only the fields that were sent. A null field means "keep the current value". */
    public User updateProfile(User user, Requests.ProfileUpdate p) {
        if (p == null) {
            throw ApiException.badRequest("Profile details are required.");
        }
        if (p.name() != null) {
            user.name = Validation.requireText(p.name(), "Full name", 100);
        }
        if (p.phone() != null) {
            user.phone = Validation.optionalText(p.phone(), "Phone", 20);
        }
        if (p.department() != null) {
            user.department = Validation.optionalText(p.department(), "Department", 100);
        }
        if (AuthService.STUDENT.equals(user.role)) {
            if (p.year() != null) {
                user.year = Validation.oneOfOrDefault(p.year(), "Year", Validation.YEARS, "");
            }
            if (p.identifier() != null) {
                String roll = Validation.requireText(p.identifier(), "Roll / Register No.", 30)
                        .toUpperCase(Locale.ROOT);
                boolean taken = users.findByRollNo(roll).filter(other -> !other.id.equals(user.id)).isPresent();
                if (taken) {
                    throw ApiException.conflict("This roll / register number is already registered.");
                }
                user.rollNo = roll;
            }
        } else if (p.identifier() != null) {
            user.employeeId = Validation.optionalText(p.identifier(), "Employee ID", 30);
        }
        if (p.profilePhoto() != null && p.profilePhoto().data() != null && !p.profilePhoto().data().isBlank()) {
            String oldPhotoId = user.profilePhotoFileId;
            var photo = files.saveImage(user.id, p.profilePhoto(), FileService.MAX_PHOTO_BYTES);
            user.profilePhotoFileId = photo.id;
            user.profilePhotoUrl = FileService.urlFor(photo.id);
            files.deleteQuietly(oldPhotoId);
        }
        user.updatedAt = Instant.now();
        return users.save(user);
    }

    public User updateSettings(User user, Requests.Settings s) {
        if (s == null) {
            throw ApiException.badRequest("Settings are required.");
        }
        UserSettings current = user.settings != null ? user.settings : new UserSettings();
        if (s.systemAlerts() != null) {
            current.systemAlerts = s.systemAlerts();
        }
        if (s.complaintUpdates() != null) {
            current.complaintUpdates = s.complaintUpdates();
        }
        if (s.eventInvitations() != null) {
            current.eventInvitations = s.eventInvitations();
        }
        if (s.directoryVisibility() != null) {
            current.directoryVisibility = s.directoryVisibility();
        }
        user.settings = current;
        user.updatedAt = Instant.now();
        return users.save(user);
    }

    /** Changes the password and signs the user out everywhere (all sessions are removed). */
    public void changePassword(User user, Requests.ChangePassword r) {
        if (r == null) {
            throw ApiException.badRequest("Password details are required.");
        }
        if (user.passwordHash == null || user.passwordHash.isEmpty()) {
            throw ApiException.badRequest("This account signs in with Google and has no password to change.");
        }
        if (!PasswordHasher.matches(r.currentPassword(), user.passwordHash)) {
            throw ApiException.badRequest("Current password is incorrect.");
        }
        String next = Validation.requirePassword(r.newPassword());
        user.passwordHash = PasswordHasher.hash(next);
        user.updatedAt = Instant.now();
        users.save(user);
        sessions.deleteByUserId(user.id);
    }
}
