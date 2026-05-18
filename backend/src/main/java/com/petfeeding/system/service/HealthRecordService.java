package com.petfeeding.system.service;

import com.petfeeding.system.model.HealthRecord;
import com.petfeeding.system.model.Pet;
import com.petfeeding.system.repository.HealthRecordRepository;
import com.petfeeding.system.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HealthRecordService {
    @Autowired
    private HealthRecordRepository healthRecordRepository;

    @Autowired
    private HealthAlertService healthAlertService;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private FeedingPlanService feedingPlanService;

    public HealthRecord save(HealthRecord healthRecord) {
        if (healthRecord.getPet() == null && healthRecord.getPetId() != null) {
            healthRecord.setPet(petRepository.findById(healthRecord.getPetId())
                    .orElseThrow(() -> new IllegalArgumentException("宠物不存在")));
        }
        if (healthRecord.getDate() == null) {
            healthRecord.setDate(new Date());
        }

        syncPetWeight(healthRecord);

        HealthRecord savedRecord = healthRecordRepository.save(healthRecord);
        
        // 检查健康状态并生成预警
        healthAlertService.checkHealthStatus(savedRecord);
        
        return savedRecord;
    }

    public List<HealthRecord> findByPetId(Long petId) {
        return healthRecordRepository.findByPet_IdOrderByDateAsc(petId);
    }

    private void syncPetWeight(HealthRecord healthRecord) {
        Pet pet = healthRecord.getPet();
        if (pet == null || pet.getId() == null || healthRecord.getWeight() == null || healthRecord.getWeight() <= 0) {
            return;
        }

        pet.setWeight(healthRecord.getWeight());
        Pet updatedPet = petRepository.save(pet);
        healthRecord.setPet(updatedPet);
        feedingPlanService.generateFeedingPlan(updatedPet);
    }
}
