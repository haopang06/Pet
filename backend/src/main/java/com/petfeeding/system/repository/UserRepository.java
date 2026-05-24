package com.petfeeding.system.repository;

import com.petfeeding.system.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByPhone(String phone);
    User findByEmail(String email);
}
