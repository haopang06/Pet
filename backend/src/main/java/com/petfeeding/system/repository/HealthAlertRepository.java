package com.petfeeding.system.repository;

import com.petfeeding.system.model.HealthAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HealthAlertRepository extends JpaRepository<HealthAlert, Long> {
    List<HealthAlert> findByPetId(Long petId);
    List<HealthAlert> findByPet_IdOrderByDateDesc(Long petId);
    Optional<HealthAlert> findFirstByPet_IdAndMessageOrderByDateDesc(Long petId, String message);
}
