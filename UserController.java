package edu.rec.campusconnect.controller;

import edu.rec.campusconnect.common.ApiBody;
import edu.rec.campusconnect.dto.Requests;
import edu.rec.campusconnect.model.User;
import edu.rec.campusconnect.service.AuthService;
import edu.rec.campusconnect.service.UserService;
import edu.rec.campusconnect.view.UserView;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The logged-in user's own account.
 *
 *   GET  /api/users/me             who am I?
 *   PUT  /api/users/me             update name, phone, roll number, photo ...
 *   PUT  /api/users/me/settings    notification and privacy switches
 *   POST /api/users/me/password    change password (signs out everywhere)
 */
@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final AuthService auth;
    private final UserService users;

    public UserController(AuthService auth, UserService users) {
        this.auth = auth;
        this.users = users;
    }

    @GetMapping
    public Map<String, Object> me(@RequestHeader(value = "Authorization", required = false) String authorization) {
        User user = auth.requireUser(authorization);
        return ApiBody.ok().with("user", UserView.from(user)).build();
    }

    @PutMapping
    public Map<String, Object> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Requests.ProfileRequest body) {
        User user = auth.requireUser(authorization);
        User updated = users.updateProfile(user, body.profile());
        return ApiBody.ok().with("user", UserView.from(updated)).build();
    }

    @PutMapping("/settings")
    public Map<String, Object> updateSettings(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Requests.SettingsRequest body) {
        User user = auth.requireUser(authorization);
        User updated = users.updateSettings(user, body.settings());
        return ApiBody.ok().with("settings", UserView.from(updated).settings()).build();
    }

    @PostMapping("/password")
    public Map<String, Object> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody Requests.ChangePassword body) {
        User user = auth.requireUser(authorization);
        users.changePassword(user, body);
        return ApiBody.ok().build();
    }
}
