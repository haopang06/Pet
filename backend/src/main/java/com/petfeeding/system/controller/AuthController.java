package com.petfeeding.system.controller;

import com.petfeeding.system.model.User;
import com.petfeeding.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            String username = normalize(user.getUsername());
            String password = user.getPassword();
            String email = normalize(user.getEmail());

            if (username.isEmpty() || password == null || password.trim().isEmpty() || email.isEmpty()) {
                return ResponseEntity.badRequest().body(error("用户名、密码和邮箱不能为空"));
            }

            user.setUsername(username);
            user.setEmail(email);

            User existingUser = userService.findByUsername(user.getUsername());
            if (existingUser != null) {
                return ResponseEntity.badRequest().body(error("用户名已存在"));
            }

            User existingEmail = userService.findByEmail(user.getEmail());
            if (existingEmail != null) {
                return ResponseEntity.badRequest().body(error("邮箱已被注册"));
            }

            User registeredUser = userService.register(user);
            registeredUser.setPassword(null);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error("注册失败，请换一个用户名或邮箱后重试"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = normalize(credentials.get("username"));
        String password = credentials.get("password");

        try {
            if (username.isEmpty() || password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(error("用户名和密码不能为空"));
            }

            User user = userService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body(error("用户不存在"));
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.badRequest().body(error("密码错误"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("token", "dummy-token-for-" + username);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error("登录失败，请稍后重试"));
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private Map<String, String> error(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("message", message);
        return error;
    }
}
