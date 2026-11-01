package edu.rec.campusconnect.model;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An uploaded image (event poster or profile photo), stored inside MongoDB in the "files" collection.
 * This replaces Google Drive, which the Apps Script version used.
 */
@Document(collection = "files")
public class StoredFile {

    @Id
    public String id;

    public String ownerId;
    public String contentType;
    public String fileName;
    public long size;

    public byte[] data;

    public Instant createdAt;

    public StoredFile() {
    }
}
