package com.petfeeding.system.service;

import com.petfeeding.system.model.HealthAlert;
import com.petfeeding.system.model.HealthRecord;
import com.petfeeding.system.repository.HealthAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HealthAlertService {
    @Autowired
    private HealthAlertRepository healthAlertRepository;

    public void checkHealthStatus(HealthRecord healthRecord) {
        // 检查体重异常
        checkWeightStatus(healthRecord);
        
        // 检查精神状态
        checkMentalState(healthRecord);
        
        // 检查排便情况
        checkDefecation(healthRecord);
    }

    private void checkWeightStatus(HealthRecord healthRecord) {
        // 这里可以实现体重异常的检查逻辑
        // 例如：与历史体重比较，检查是否有异常变化
    }

    private void checkMentalState(HealthRecord healthRecord) {
        if ("poor".equals(healthRecord.getMentalState())) {
            HealthAlert alert = new HealthAlert();
            alert.setPet(healthRecord.getPet());
            alert.setMessage("宠物精神状态不佳，建议关注");
            alert.setDate(new Date());
            alert.setSeverity("medium");
            healthAlertRepository.save(alert);
        }
    }

    private void checkDefecation(HealthRecord healthRecord) {
        if ("diarrhea".equals(healthRecord.getDefecation())) {
            HealthAlert alert = new HealthAlert();
            alert.setPet(healthRecord.getPet());
            alert.setMessage("宠物出现腹泻，建议及时就医");
            alert.setDate(new Date());
            alert.setSeverity("high");
            healthAlertRepository.save(alert);
        } else if ("hard".equals(healthRecord.getDefecation())) {
            HealthAlert alert = new HealthAlert();
            alert.setPet(healthRecord.getPet());
            alert.setMessage("宠物排便偏硬，建议增加水分摄入");
            alert.setDate(new Date());
            alert.setSeverity("low");
            healthAlertRepository.save(alert);
        }
    }

    public List<HealthAlert> findByPetId(Long petId) {
        return healthAlertRepository.findByPetId(petId);
    }
}