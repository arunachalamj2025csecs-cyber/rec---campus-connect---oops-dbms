package edu.rec.campusconnect.service;

import edu.rec.campusconnect.common.ApiException;
import edu.rec.campusconnect.common.Validation;
import edu.rec.campusconnect.config.AppConfig;
import edu.rec.campusconnect.dto.Requests;
import edu.rec.campusconnect.model.User;
import edu.rec.campusconnect.model.UserSession;
import edu.rec.campusconnect.model.UserSettings;
import edu.rec.campusconnect.repository.UserRepository;
import edu.rec.campusconnect.repository.UserSessionRepository;
import edu.rec.campusconnect.security.PasswordHasher;
import edu.rec.campusconnect.security.Tokens;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import edu.rec.campusconnect.service.GoogleTokenVerifier.GoogleProfile;

/**
 * Registration, login, Google sign-in, sessions and role checks.
 *
 * How a session works:
 * 1. Login creates a random token and sends it to the browser once.
 * 2. The browser sends it back in the header  Authorization: Bearer <token>.
 * 3. requireUser() hashes the token and looks the hash up in the sessions collection.
 */
@Service
public class AuthService {

    public static final String STUDENT = "student";
    public static final String ADMIN = "admin";
    private static final String ACTIVE = "active";

    private final UserRepository users;
    private final UserSessionRepository sessions;
    private final AppConfig config;
    private final GoogleTokenVerifier googleVerifier;

    public AuthService(UserRepository users, UserSessionRepository sessions,
                       AppConfig config, GoogleTokenVerifier googleVerifier) {
        this.users = users;
        this.sessions = sessions;
        this.config = config;
        this.googleVerifier = googleVerifier;
    }

    /** What a successful login returns: the token for the browser and the user it belongs to. */
    public record Result(String token, User user) {
    }

    // ---------------------------------------------------------------- registration

    public User registerStudent(Requests.RegisterStudent r) {
        if (r == null) {
            throw ApiException.badRequest("Registration details are required.");
        }
        String name = Validation.requireText(r.name(), "Full name", 100);
        String email = requireStudentEmail(r.email());
        String password = Validation.requirePassword(r.password());
        String rollNo = Validation.requireText(r.rollNo(), "Roll / Register No.", 30).toUpperCase(Locale.ROOT);
        String department = Validation.requireText(r.department(), "Department", 100);
        String year = Validation.oneOfOrDefault(r.year(), "Year", Validation.YEARS, "");
        String phone = Validation.optionalText(r.phone(), "Phone", 20);

        if (users.findByEmail(email).isPresent()) {
            throw ApiException.conflict("An account already exists with this email.");
        }
        if (users.findByRollNo(rollNo).isPresent()) {
            throw ApiException.conflict("This roll / register number is already registered.");
        }

        User user = newUser(STUDENT, name, email);
        user.passwordHash = PasswordHasher.hash(password);
        user.phone = phone;
        user.department = department;
        user.year = year;
        user.rollNo = rollNo;
        return users.save(user);
    }

    /**
     * Admin accounts need two things: the email must be on the ADMIN_EMAILS list,
     * and the person must type the ADMIN_REGISTRATION_KEY secret.
     */
    public User registerAdmin(Requests.RegisterAdmin r) {
        if (r == null) {
            throw ApiException.badRequest("Registration details are required.");
        }
        String configuredKey = config.adminRegistrationKey();
        if (configuredKey.isEmpty()) {
            throw ApiException.forbidden("Admin registration is turned off. The server needs ADMIN_REGISTRATION_KEY set.");
        }
        String givenKey = r.adminKey() == null ? "" : r.adminKey();
        if (!sameSecret(configuredKey, givenKey)) {
            throw ApiException.forbidden("Invalid admin registration password.");
        }
        String name = Validation.requireText(r.name(), "Full name", 100);
        String email = requireEmail(r.email());
        if (!config.isAdminEmail(email)) {
            throw ApiException.forbidden("This email is not on the administrator list (ADMIN_EMAILS).");
        }
        String password = Validation.requirePassword(r.password());
        if (users.findByEmail(email).isPresent()) {
            throw ApiException.conflict("An account already exists with this email.");
        }

        User admin = newUser(ADMIN, name, email);
        admin.passwordHash = PasswordHasher.hash(password);
        admin.phone = Validation.optionalText(r.phone(), "Phone", 20);
        admin.department = Validation.optionalText(r.department(), "Department", 100);
        if (admin.department.isEmpty()) {
            admin.department = "Administration";
        }
        return users.save(admin);
    }

    // ---------------------------------------------------------------- login

    /** Email + password login for students and administrators. */
    public Result login(Requests.Login r) {
        if (r == null) {
            throw ApiException.badRequest("Email and password are required.");
        }
        String role = normalizeRole(r.role());
        String email = Validation.normalizeEmail(r.email());
        assertEmailAllowedForRole(email, role);

        Optional<User> found = users.findByEmail(email);
        User user = found.filter(u -> role.equals(u.role))
                .orElseThrow(() -> ApiException.unauthorized("Invalid email, password or portal type."));
        if (!ACTIVE.equals(user.status)) {
            throw ApiException.forbidden("This account is inactive.");
        }
        if (!PasswordHasher.matches(r.password(), user.passwordHash)) {
            throw ApiException.unauthorized("Invalid email or password.");
        }
        return startSession(user);
    }

    /** Google sign-in. The browser sends a Google ID token; we ask Google to verify it. */
    public Result googleLogin(Requests.GoogleLogin r) {
        if (r == null) {
            throw ApiException.badRequest("Google credential is required.");
        }
        String role = normalizeRole(r.role());
        GoogleProfile profile = googleVerifier.verify(r.credential());
        assertEmailAllowedForRole(profile.email(), role);

        Optional<User> bySub = users.findByGoogleSub(profile.sub());
        User user = bySub.orElseGet(() -> users.findByEmail(profile.email()).orElse(null));
        Instant now = Instant.now();

        if (user != null) {
            if (!role.equals(user.role)) {
                throw ApiException.forbidden(STUDENT.equals(role)
                        ? "This Google account is linked to an administrator account. Use Admin Login."
                        : "This Google account is not an administrator account.");
            }
            if (!ACTIVE.equals(user.status)) {
                throw ApiException.forbidden("This account is inactive.");
            }
            user.googleSub = profile.sub();
            if (user.name == null || user.name.isBlank()) {
                user.name = profile.name();
            }
            user.updatedAt = now;
            users.save(user);
        } else {
            user = newUser(role, profile.name(), profile.email());
            user.googleSub = profile.sub();
            if (ADMIN.equals(role)) {
                user.department = "Administration";
            }
            users.save(user);
        }
        return startSession(user);
    }

    public void logout(String authorizationHeader) {
        String token = Tokens.bearerToken(authorizationHeader);
        if (token != null) {
            sessions.deleteById(Tokens.sha256Hex(token));
        }
    }

    // ---------------------------------------------------------------- sessions and roles

    /** Returns the logged-in user, or throws 401 when the token is missing, unknown or expired. */
    public User requireUser(String authorizationHeader) {
        String token = Tokens.bearerToken(authorizationHeader);
        if (token == null) {
            throw ApiException.unauthorized("Session expired. Please log in again.");
        }
        String hash = Tokens.sha256Hex(token);
        UserSession session = sessions.findById(hash)
                .orElseThrow(() -> ApiException.unauthorized("Session expired. Please log in again."));
        if (session.expiresAt == null || Instant.now().isAfter(session.expiresAt)) {
            sessions.deleteById(hash);
            throw ApiException.unauthorized("Session expired. Please log in again.");
        }
        User user = users.findById(session.userId)
                .orElseThrow(() -> ApiException.unauthorized("Account not found."));
        if (!ACTIVE.equals(user.status)) {
            throw ApiException.forbidden("Account is inactive.");
        }
        return user;
    }

    /** Like requireUser, but also checks the role ("student" or "admin"). */
    public User requireRole(String authorizationHeader, String role) {
        User user = requireUser(authorizationHeader);
        if (!role.equals(user.role)) {
            throw ApiException.forbidden("You do not have permission for this action.");
        }
        return user;
    }

    // ---------------------------------------------------------------- helpers

    private Result startSession(User user) {
        Instant now = Instant.now();
        String token = Tokens.newSessionToken();

        UserSession session = new UserSession();
        session.tokenHash = Tokens.sha256Hex(token);
        session.userId = user.id;
        session.role = user.role;
        session.createdAt = now;
        session.expiresAt = now.plus(Duration.ofHours(config.sessionHours()));
        sessions.save(session);

        user.lastLoginAt = now;
        users.save(user);
        return new Result(token, user);
    }

    private User newUser(String role, String name, String email) {
        Instant now = Instant.now();
        User user = new User();
        user.id = Tokens.readableId(ADMIN.equals(role) ? "ADM" : "STU");
        user.role = role;
        user.name = name;
        user.email = email;
        user.status = ACTIVE;
        user.settings = new UserSettings();
        user.createdAt = now;
        user.updatedAt = now;
        return user;
    }

    private void assertEmailAllowedForRole(String email, String role) {
        if (email.isEmpty()) {
            throw ApiException.badRequest("Email is required.");
        }
        if (STUDENT.equals(role)) {
            if (!Validation.isStudentEmail(email)) {
                throw ApiException.badRequest("Use your college email address ending in @rajalakshmi.edu.in.");
            }
            return;
        }
        if (!config.isAdminEmail(email)) {
            throw ApiException.forbidden("Admin access is restricted to the administrator list (ADMIN_EMAILS).");
        }
    }

    private static String requireStudentEmail(String raw) {
        String email = requireEmail(raw);
        if (!Validation.isStudentEmail(email)) {
            throw ApiException.badRequest("Use your college email address ending in @rajalakshmi.edu.in.");
        }
        return email;
    }

    private static String requireEmail(String raw) {
        String email = Validation.normalizeEmail(raw);
        if (email.isEmpty()) {
            throw ApiException.badRequest("Email is required.");
        }
        return email;
    }

    private static String normalizeRole(String raw) {
        String role = Validation.trimmed(raw).isEmpty() ? STUDENT : raw.trim().toLowerCase(Locale.ROOT);
        if (!STUDENT.equals(role) && !ADMIN.equals(role)) {
            throw ApiException.badRequest("Invalid portal type.");
        }
        return role;
    }

    /** Compares two secrets without revealing, through timing, how many characters matched. */
    private static boolean sameSecret(String expected, String given) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), given.getBytes(StandardCharsets.UTF_8));
    }
}
