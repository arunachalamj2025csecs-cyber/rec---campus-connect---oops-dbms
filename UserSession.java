package edu.rec.campusconnect.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A logged-in session, stored in the "sessions" collection.
 *
 * The _id is the SHA-256 hash of the token the browser holds. The raw token is never stored,
 * so someone who reads the database cannot log in with it.
 */
@Document(collection = "sessions")
public class UserSession {

    @Id
    public String tokenHash;

    @Indexed
    public String userId;

    public String role;
    public Instant createdAt;
    /** MongoDB removes the session automatically once expiresAt has passed (TTL index). */
    @Indexed(expireAfterSeconds = 0)
    public Instant expiresAt;

    public UserSession() {
    }
}
