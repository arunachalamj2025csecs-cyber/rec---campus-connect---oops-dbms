package edu.rec.campusconnect.controller;

import edu.rec.campusconnect.common.ApiBody;
import edu.rec.campusconnect.dto.Requests;
import edu.rec.campusconnect.model.User;
import edu.rec.campusconnect.service.AuthService;
import edu.rec.campusconnect.service.EventService;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Campus events.
 *
 *   GET    /api/events           events board (students: published only)
 *   GET    /api/events/{id}      one event
 *   POST   /api/events           admin creates an event
 *   PUT    /api/events/{id}      admin edits or publishes an event
 *   DELETE /api/events/{id}      admin deletes an event and its poster
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final AuthService auth;
    private final EventService events;

    public EventController(AuthService auth, EventService events) {
        this.auth = auth;
        this.events = events;
    }

    @GetMapping
    public Map<String, Object> list(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User viewer = auth.requireUser(authorization);
        return ApiBody.ok().with("events", events.list(viewer)).build();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("id") String id) {
        User viewer = auth.requireUser(authorization);
        return ApiBody.ok().with("event", events.get(viewer, id)).build();
    }

    @PostMapping
    public Map<String, Object> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Requests.EventRequest body) {
        User admin = auth.requireRole(authorization, AuthService.ADMIN);
        return ApiBody.ok().with("event", events.create(admin, body.event())).build();
    }

    @PutMapping("/{id}")
    public Map<String, Object> update(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("id") String id,
            @RequestBody Requests.EventRequest body) {
        User admin = auth.requireRole(authorization, AuthService.ADMIN);
        return ApiBody.ok().with("event", events.update(admin, id, body.event())).build();
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("id") String id) {
        auth.requireRole(authorization, AuthService.ADMIN);
        events.delete(id);
        return ApiBody.ok().build();
    }
}
