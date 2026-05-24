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
    private static final String PHONE_PATTERN = "^1\\d{10}$";

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            String username = normalize(request.getUsername());
            String phone = normalize(request.getPhone());
            String password = request.getPassword();
            String confirmPassword = request.getConfirmPassword();

            if (username.isEmpty() || phone.isEmpty() || password == null || password.trim().isEmpty() || confirmPassword == null || confirmPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(error("用户名、手机号、密码和确认密码不能为空"));
            }

            if (!phone.matches(PHONE_PATTERN)) {
                return ResponseEntity.badRequest().body(error("请输入有效的 11 位手机号"));
            }

            if (!password.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(error("两次输入的密码不一致"));
            }

            if (userService.findByUsername(username) != null) {
                return ResponseEntity.badRequest().body(error("用户名已存在"));
            }

            if (userService.findByPhone(phone) != null) {
                return ResponseEntity.badRequest().body(error("手机号已被注册"));
            }

            User user = new User();
            user.setUsername(username);
            user.setPhone(phone);
            user.setPassword(password);
            User registeredUser = userService.register(user);
            registeredUser.setPassword(null);
            return ResponseEntity.ok(registeredUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error("注册失败，请检查输入信息后重试"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String account = normalize(credentials.get("username"));
        String password = credentials.get("password");

        try {
            if (account.isEmpty() || password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(error("用户名和密码不能为空"));
            }

            User user = userService.findByUsername(account);
            if (user == null) {
                user = userService.findByPhone(account);
            }
            if (user == null) {
                return ResponseEntity.badRequest().body(error("用户不存在"));
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.badRequest().body(error("密码错误"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("token", "dummy-token-for-" + user.getUsername());
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("phone", user.getPhone());
            response.put("avatar", user.getAvatar());
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

    public static class RegisterRequest {
        private String username;
        private String phone;
        private String password;
        private String confirmPassword;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
