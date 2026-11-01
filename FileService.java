package edu.rec.campusconnect.service;

import edu.rec.campusconnect.common.ApiException;
import edu.rec.campusconnect.dto.Requests;
import edu.rec.campusconnect.model.StoredFile;
import edu.rec.campusconnect.repository.StoredFileRepository;
import edu.rec.campusconnect.rules.FileRules;
import edu.rec.campusconnect.security.Tokens;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Saves uploaded images (posters and profile photos) inside MongoDB.
 * Each image gets a random, hard-to-guess ID, so its link can be shared the same way
 * the Google Drive links were in the Apps Script version.
 */
@Service
public class FileService {

    public static final long MAX_POSTER_BYTES = 5L * 1024 * 1024;
    public static final long MAX_PHOTO_BYTES = 3L * 1024 * 1024;

    private final StoredFileRepository files;

    public FileService(StoredFileRepository files) {
        this.files = files;
    }

    /** The relative link saved on users and events, e.g. /api/files/AbC123. */
    public static String urlFor(String fileId) {
        return "/api/files/" + fileId;
    }

    public StoredFile saveImage(String ownerId, Requests.Upload upload, long maxBytes) {
        if (upload == null || upload.data() == null || upload.data().isBlank()) {
            throw ApiException.badRequest("Image data is missing.");
        }
        String type = FileRules.normalizeImageType(upload.type());
        if (type == null) {
            throw ApiException.badRequest("Only PNG, JPG or WEBP images are allowed.");
        }
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(upload.data().trim());
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("The image could not be read. Please choose the file again.");
        }
        if (bytes.length > maxBytes) {
            throw ApiException.badRequest("Image must be " + (maxBytes / (1024 * 1024)) + " MB or smaller.");
        }
        if (!FileRules.matchesSignature(type, bytes)) {
            throw ApiException.badRequest("The file content does not match its image type.");
        }

        StoredFile file = new StoredFile();
        file.id = Tokens.newFileId();
        file.ownerId = ownerId;
        file.contentType = type;
        file.fileName = safeName(upload.name());
        file.size = bytes.length;
        file.data = bytes;
        file.createdAt = Instant.now();
        return files.save(file);
    }

    public Optional<StoredFile> find(String fileId) {
        return files.findById(fileId);
    }

    /** Deletes an old image. Does nothing when there is no ID. */
    public void deleteQuietly(String fileId) {
        if (fileId != null && !fileId.isBlank()) {
            files.deleteById(fileId);
        }
    }

    /** Removes characters that could cause trouble in a file name. */
    static String safeName(String name) {
        String cleaned = name == null || name.isBlank() ? "image" : name.replaceAll("[^A-Za-z0-9._-]", "_");
        return cleaned.length() > 100 ? cleaned.substring(cleaned.length() - 100) : cleaned;
    }
}
