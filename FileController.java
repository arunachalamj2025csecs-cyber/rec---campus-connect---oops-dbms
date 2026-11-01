package edu.rec.campusconnect.controller;

import edu.rec.campusconnect.common.ApiException;
import edu.rec.campusconnect.model.StoredFile;
import edu.rec.campusconnect.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves uploaded images (posters and profile photos).
 * Images are public by link, like the Google Drive links were. The IDs are random and hard to guess.
 *
 *   GET /api/files/{id}
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService files;

    public FileController(FileService files) {
        this.files = files;
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> download(@PathVariable("id") String id) {
        StoredFile file = files.find(id).orElseThrow(() -> ApiException.notFound("File not found."));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType))
                .header("Cache-Control", "public, max-age=86400")
                .header("X-Content-Type-Options", "nosniff")
                .body(file.data);
    }
}
