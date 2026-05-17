package com.petfeeding.system.repository;

import com.petfeeding.system.model.HealthAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthAlertRepository extends JpaRepository<HealthAlert, Long> {
    List<HealthAlert> findByPetId(Long petId);
}