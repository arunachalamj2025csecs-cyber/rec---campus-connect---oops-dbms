package edu.rec.campusconnect.repository;

import edu.rec.campusconnect.model.Event;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Database access for events. findAll, findById, save and deleteById come from MongoRepository. */
public interface EventRepository extends MongoRepository<Event, String> {
}
