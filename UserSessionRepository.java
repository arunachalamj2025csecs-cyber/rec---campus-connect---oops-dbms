package edu.rec.campusconnect.repository;

import edu.rec.campusconnect.model.UserSession;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Database access for login sessions. The ID is the token hash. */
public interface UserSessionRepository extends MongoRepository<UserSession, String> {

    /** Removes every session of one user (used after a password change). */
    void deleteByUserId(String userId);
}
