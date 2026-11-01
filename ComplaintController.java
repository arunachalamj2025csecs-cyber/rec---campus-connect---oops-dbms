package edu.rec.campusconnect.controller;

import edu.rec.campusconnect.common.ApiBody;
import edu.rec.campusconnect.dto.Requests;
import edu.rec.campusconnect.model.User;
import edu.rec.campusconnect.service.AuthService;
import edu.rec.campusconnect.service.ComplaintService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Complaints.
 *
 *   GET  /api/complaints?status=all|open|inprogress|resolved   list (students: own, admins: all)
 *   GET  /api/complaints/search?q=text                         search the list
 *   GET  /api/complaints/summary                               admin counts by status and category
 *   POST /api/complaints                                       student files a complaint
 *   GET  /api/complaints/{id}                                  view one complaint
 *   PUT  /api/complaints/{id}                                  admin changes status and note
 */
@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final AuthService auth;
    private final ComplaintService complaints;

    public ComplaintController(AuthService auth, ComplaintService complaints) {
        this.auth = auth;
        this.complaints = complaints;
    }

    @GetMapping
    public Map<String, Object> list(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "status", required = false) String status) {
        User viewer = auth.requireUser(authorization);
        return ApiBody.ok().with("complaints", complaints.list(viewer, status)).build();
    }

    @GetMapping("/search")
    public Map<String, Object> search(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(value = "q", required = false) String query) {
        User viewer = auth.requireUser(authorization);
        return ApiBody.ok().with("complaints", complaints.search(viewer, query)).build();
    }

    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestHeader(value = "Authorization", required = false) String authorization) {
        auth.requireRole(authorization, AuthService.ADMIN);
        return ApiBody.ok().with("summary", complaints.summary()).build();
    }

    @PostMapping
    public Map<String, Object> create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Requests.CreateComplaint body) {
        User student = auth.requireRole(authorization, AuthService.STUDENT);
        return ApiBody.ok().with("complaint", complaints.create(student, body.complaint())).build();
    }

    @GetMapping("/{id}")
    public Map<String, Object> get(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("id") String id) {
        User viewer = auth.requireUser(authorization);
        return ApiBody.ok().with("complaint", complaints.get(viewer, id)).build();
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateStatus(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("id") String id,
            @RequestBody Requests.UpdateComplaint body) {
        User admin = auth.requireRole(authorization, AuthService.ADMIN);
        return ApiBody.ok().with("complaint", complaints.updateStatus(admin, id, body)).build();
    }
}
