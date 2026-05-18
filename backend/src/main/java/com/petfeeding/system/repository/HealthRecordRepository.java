package com.petfeeding.system.repository;

import com.petfeeding.system.model.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {
    List<HealthRecord> findByPet_Id(Long petId);
    List<HealthRecord> findByPet_IdOrderByDateAsc(Long petId);
    List<HealthRecord> findTop3ByPet_IdOrderByDateDesc(Long petId);
}
