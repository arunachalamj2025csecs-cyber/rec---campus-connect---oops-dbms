package edu.rec.campusconnect.controller;

import edu.rec.campusconnect.common.ApiBody;
import edu.rec.campusconnect.dto.Requests;
import edu.rec.campusconnect.model.User;
import edu.rec.campusconnect.service.AuthService;
import edu.rec.campusconnect.view.UserView;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registration, login, Google sign-in and logout.
 *
 * Endpoints:
 *   POST /api/auth/register/student
 *   POST /api/auth/register/admin
 *   POST /api/auth/login
 *   POST /api/auth/google
 *   POST /api/auth/logout
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register/student")
    public ResponseEntity<Map<String, Object>> registerStudent(@RequestBody Requests.RegisterStudent body) {
        User user = auth.registerStudent(body);
        return ResponseEntity.status(201).body(ApiBody.ok().with("user", UserView.from(user)).build());
    }

    @PostMapping("/register/admin")
    public ResponseEntity<Map<String, Object>> registerAdmin(@RequestBody Requests.RegisterAdmin body) {
        User user = auth.registerAdmin(body);
        return ResponseEntity.status(201).body(ApiBody.ok().with("user", UserView.from(user)).build());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Requests.Login body) {
        return sessionBody(auth.login(body));
    }

    @PostMapping("/google")
    public Map<String, Object> googleLogin(@RequestBody Requests.GoogleLogin body) {
        return sessionBody(auth.googleLogin(body));
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        auth.logout(authorization);
        return ApiBody.ok().build();
    }

    private static Map<String, Object> sessionBody(AuthService.Result result) {
        return ApiBody.ok()
                .with("session", result.token())
                .with("user", UserView.from(result.user()))
                .build();
    }
}
