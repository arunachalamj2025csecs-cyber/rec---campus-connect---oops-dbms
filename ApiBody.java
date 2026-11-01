package edu.rec.campusconnect.common;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds the JSON reply for successful requests.
 * Every successful reply looks like: { "ok": true, ...more fields... }
 *
 * Example:  return ApiBody.ok().with("user", view).build();
 */
public final class ApiBody {

    private final Map<String, Object> body = new LinkedHashMap<>();

    private ApiBody() {
        body.put("ok", true);
    }

    public static ApiBody ok() {
        return new ApiBody();
    }

    public ApiBody with(String key, Object value) {
        body.put(key, value);
        return this;
    }

    public Map<String, Object> build() {
        return body;
    }

    /** Builds the JSON reply for failed requests: { "ok": false, "message": "..." } */
    public static Map<String, Object> error(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", false);
        body.put("message", message);
        return body;
    }
}
