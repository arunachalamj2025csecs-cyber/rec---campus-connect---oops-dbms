package edu.rec.campusconnect.controller;

import edu.rec.campusconnect.common.ApiBody;
import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** GET /api/health - a quick "is the server awake?" check. Hosting services use it too. */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public Map<String, Object> health() {
        return ApiBody.ok().with("service", "campus-connect-api").with("time", Instant.now()).build();
    }
}
