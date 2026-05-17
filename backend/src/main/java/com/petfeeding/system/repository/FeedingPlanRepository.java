package com.petfeeding.system.repository;

import com.petfeeding.system.model.FeedingPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedingPlanRepository extends JpaRepository<FeedingPlan, Long> {
    Optional<FeedingPlan> findByPetId(Long petId);
}