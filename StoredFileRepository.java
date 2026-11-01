package edu.rec.campusconnect.repository;

import edu.rec.campusconnect.model.StoredFile;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Database access for uploaded images. */
public interface StoredFileRepository extends MongoRepository<StoredFile, String> {
}
