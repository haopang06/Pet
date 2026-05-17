package com.petfeeding.system.service;

import com.petfeeding.system.model.HealthRecord;
import com.petfeeding.system.repository.HealthRecordRepository;
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

    public HealthRecord save(HealthRecord healthRecord) {
        healthRecord.setDate(new Date());
        HealthRecord savedRecord = healthRecordRepository.save(healthRecord);
        
        // 检查健康状态并生成预警
        healthAlertService.checkHealthStatus(savedRecord);
        
        return savedRecord;
    }

    public List<HealthRecord> findByPetId(Long petId) {
        return healthRecordRepository.findByPetId(petId);
    }
}