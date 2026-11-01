package edu.rec.campusconnect.repository;

import edu.rec.campusconnect.model.Complaint;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

/** Database access for complaints. */
public interface ComplaintRepository extends MongoRepository<Complaint, String> {

    List<Complaint> findByStudentId(String studentId);
}
