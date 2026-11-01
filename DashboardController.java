package edu.rec.campusconnect.controller;

import edu.rec.campusconnect.model.User;
import edu.rec.campusconnect.service.AuthService;
import edu.rec.campusconnect.service.DashboardService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** GET /api/dashboard - the numbers and short lists for the home page. */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AuthService auth;
    private final DashboardService dashboard;

    public DashboardController(AuthService auth, DashboardService dashboard) {
        this.auth = auth;
        this.dashboard = dashboard;
    }

    @GetMapping
    public Map<String, Object> dashboard(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User viewer = auth.requireUser(authorization);
        return dashboard.build(viewer);
    }
}
