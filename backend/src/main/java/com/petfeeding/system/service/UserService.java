package com.petfeeding.system.service;

import com.petfeeding.system.model.User;
import com.petfeeding.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            user.setEmail(buildInternalEmail(user));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User updateAvatar(Long userId, String avatar) {
        User user = requireUser(userId);
        user.setAvatar(normalizeNullable(avatar));
        return userRepository.save(user);
    }

    public User updatePassword(Long userId, String oldPassword, String newPassword, String confirmPassword) {
        User user = requireUser(userId);

        if (isBlank(oldPassword) || isBlank(newPassword) || isBlank(confirmPassword)) {
            throw new IllegalArgumentException("原密码、新密码和确认密码不能为空");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("两次输入的新密码不一致");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("新密码不能与原密码相同");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public Map<String, Object> toPublicProfile(User user) {
        Map<String, Object> profile = new HashMap<>();
        if (user == null) {
            return profile;
        }

        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("phone", user.getPhone());
        profile.put("avatar", user.getAvatar());
        return profile;
    }

    private String buildInternalEmail(User user) {
        String phone = user.getPhone();
        if (phone != null && !phone.trim().isEmpty()) {
            return phone.trim() + "@pet.local";
        }

        String username = user.getUsername();
        if (username != null && !username.trim().isEmpty()) {
            return username.trim() + "@pet.local";
        }

        return "user-" + System.currentTimeMillis() + "@pet.local";
    }

    private User requireUser(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        User user = findById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}
