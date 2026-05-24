package com.petfeeding.system.config;

import com.petfeeding.system.model.User;
import com.petfeeding.system.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User existingAdmin = userRepository.findByUsername("admin");
        if (existingAdmin != null) {
            if (existingAdmin.getPhone() == null || existingAdmin.getPhone().trim().isEmpty()) {
                String adminPhone = resolveAdminPhone(existingAdmin);
                if (adminPhone != null) {
                    existingAdmin.setPhone(adminPhone);
                    userRepository.save(existingAdmin);
                }
            }
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPhone(resolveAdminPhone(null));
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setEmail(resolveAdminEmail());
        userRepository.save(admin);
    }

    private String resolveAdminPhone(User currentAdmin) {
        String phone = "13800000000";
        User existingPhoneOwner = userRepository.findByPhone(phone);
        if (existingPhoneOwner == null || (currentAdmin != null && existingPhoneOwner.getId().equals(currentAdmin.getId()))) {
            return phone;
        }
        return null;
    }

    private String resolveAdminEmail() {
        String email = "admin@pet.local";
        if (userRepository.findByEmail(email) == null) {
            return email;
        }
        return "admin-" + System.currentTimeMillis() + "@pet.local";
    }
}
