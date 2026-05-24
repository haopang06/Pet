package com.petfeeding.system.controller;

import com.petfeeding.system.model.User;
import com.petfeeding.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", "用户不存在"));
        }
        return ResponseEntity.ok(userService.toPublicProfile(user));
    }

    @RequestMapping(value = "/{id}/avatar", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<?> updateAvatar(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String avatar = payload == null ? null : payload.get("avatar");
            User updatedUser = userService.updateAvatar(id, avatar);
            return ResponseEntity.ok(userService.toPublicProfile(updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @RequestMapping(value = "/{id}/password", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody PasswordUpdateRequest request) {
        try {
            if (request == null) {
                throw new IllegalArgumentException("原密码、新密码和确认密码不能为空");
            }
            userService.updatePassword(id, request.getOldPassword(), request.getNewPassword(), request.getConfirmPassword());
            return ResponseEntity.ok(Collections.singletonMap("message", "密码修改成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    public static class PasswordUpdateRequest {
        private String oldPassword;
        private String newPassword;
        private String confirmPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
