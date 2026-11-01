package edu.rec.campusconnect.repository;

import edu.rec.campusconnect.model.User;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Database access for users. Spring writes the query for each method from its name:
 * findByEmail becomes  db.users.find({ email: ... }).
 */
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByRollNo(String rollNo);

    Optional<User> findByGoogleSub(String googleSub);
}
