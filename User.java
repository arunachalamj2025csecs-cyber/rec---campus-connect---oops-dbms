package edu.rec.campusconnect.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A student or administrator account, stored in the "users" MongoDB collection.
 *
 * Never send this object to the browser directly: it contains the password hash.
 * Use UserView instead.
 */
@Document(collection = "users")
public class User {

    @Id
    public String id;

    @Indexed(unique = true)
    public String email;

    /** "student" or "admin". */
    public String role;

    public String name;

    /** PBKDF2 hash. Null for accounts that only use Google sign-in. */
    public String passwordHash;

    public String phone;
    public String department;
    public String year;

    /** Roll / register number (students). Unique when present. */
    @Indexed(unique = true, sparse = true)
    public String rollNo;

    /** Employee ID (administrators). */
    public String employeeId;

    /** The Google account ID ("sub") once the user has signed in with Google. Unique when present. */
    @Indexed(unique = true, sparse = true)
    public String googleSub;

    public UserSettings settings = new UserSettings();

    /** "active" or "inactive". Inactive accounts cannot log in. */
    public String status = "active";

    /** Relative link such as /api/files/abc, resolved by the frontend against the API address. */
    public String profilePhotoUrl;
    public String profilePhotoFileId;

    public Instant createdAt;
    public Instant updatedAt;
    public Instant lastLoginAt;

    public User() {
    }
}
