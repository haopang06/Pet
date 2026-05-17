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
            User existingUser = userService.findByUsername(user.getUsername());
            if (existingUser != null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户名已存在");
                return ResponseEntity.badRequest().body(error);
            }
            User registeredUser = userService.register(user);
            registeredUser.setPassword(null);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "注册失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            User user = userService.findByUsername(username);
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "用户不存在");
                return ResponseEntity.badRequest().body(error);
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "密码错误");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("token", "dummy-token-for-" + username);
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "登录失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}