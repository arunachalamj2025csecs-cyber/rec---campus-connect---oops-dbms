package edu.rec.campusconnect.common;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * One place that turns errors into JSON replies: { "ok": false, "message": "..." }.
 * Internal details are logged on the server but never shown to the browser.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ApiBody.error(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(400).body(ApiBody.error("The request body is not valid JSON."));
    }

    /** Happens when two requests try to create the same unique value (for example, the same email) at once. */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateKeyException ex) {
        return ResponseEntity.status(409).body(ApiBody.error("This record already exists. Check the email or roll number."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(RuntimeException ex) {
        log.error("Unexpected server error", ex);
        return ResponseEntity.status(500).body(ApiBody.error("Something went wrong on the server. Please try again."));
    }
}
