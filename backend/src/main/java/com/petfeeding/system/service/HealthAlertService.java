package com.petfeeding.system.service;

import com.petfeeding.system.model.HealthAlert;
import com.petfeeding.system.model.HealthRecord;
import com.petfeeding.system.repository.HealthAlertRepository;
import com.petfeeding.system.repository.HealthRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class HealthAlertService {
    @Autowired
    private HealthAlertRepository healthAlertRepository;

    @Autowired
    private HealthRecordRepository healthRecordRepository;

    public void checkHealthStatus(HealthRecord healthRecord) {
        // 检查体重异常
        checkWeightStatus(healthRecord);
        
        // 检查精神状态
        checkMentalState(healthRecord);
        
        // 检查排便情况
        checkDefecation(healthRecord);

        // 检查体重和进食趋势
        checkTrendStatus(healthRecord);
    }

    private void checkWeightStatus(HealthRecord healthRecord) {
        if (healthRecord.getWeight() == null || healthRecord.getWeight() <= 0) {
            createAlert(healthRecord, "体重数据异常，请确认记录是否准确", "medium");
        }
    }

    private void checkMentalState(HealthRecord healthRecord) {
        if ("poor".equals(healthRecord.getMentalState())) {
            createAlert(healthRecord, "宠物精神状态不佳，建议关注", "medium");
        }
    }

    private void checkDefecation(HealthRecord healthRecord) {
        if ("diarrhea".equals(healthRecord.getDefecation())) {
            createAlert(healthRecord, "宠物出现腹泻，建议及时就医", "high");
        } else if ("hard".equals(healthRecord.getDefecation())) {
            createAlert(healthRecord, "宠物排便偏硬，建议增加水分摄入", "low");
        }
    }

    private void checkTrendStatus(HealthRecord healthRecord) {
        if (healthRecord.getPet() == null || healthRecord.getPet().getId() == null) {
            return;
        }

        List<HealthRecord> recentRecords = findRecentThreeDayRecords(healthRecord);
        if (recentRecords.size() < 3) {
            return;
        }

        HealthRecord first = recentRecords.get(0);
        HealthRecord second = recentRecords.get(1);
        HealthRecord third = recentRecords.get(2);

        if (!hasPositiveWeight(first) || !hasPositiveWeight(second) || !hasPositiveWeight(third)) {
            return;
        }

        boolean weightDropping = first.getWeight() > second.getWeight() && second.getWeight() > third.getWeight();
        double weightDropRatio = (first.getWeight() - third.getWeight()) / first.getWeight();
        boolean foodDropping = hasFoodIntake(first) && hasFoodIntake(second) && hasFoodIntake(third)
                && first.getFoodIntake() > second.getFoodIntake()
                && second.getFoodIntake() > third.getFoodIntake();

        if (weightDropping && weightDropRatio >= 0.05 && foodDropping) {
            createAlert(healthRecord, "检测到宠物在过去3天内体重连续下降超过5%，且进食量减少，建议及时就医", "high");
        } else if (weightDropping && weightDropRatio >= 0.05) {
            createAlert(healthRecord, "检测到宠物近期体重连续下降超过5%，建议加强观察", "medium");
        }

        if (hasWaterIntake(third)) {
            double lowWaterThreshold = Math.max(third.getWeight() * 35, 80);
            if (third.getWaterIntake() < lowWaterThreshold) {
                createAlert(healthRecord, "宠物饮水量偏低，建议关注饮水和排尿情况", "medium");
            }
        }
    }

    public List<HealthAlert> findByPetId(Long petId) {
        return healthAlertRepository.findByPet_IdOrderByDateDesc(petId);
    }

    public HealthAlert markHandled(Long alertId) {
        HealthAlert alert = healthAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("健康预警不存在"));
        alert.setHandled(true);
        return healthAlertRepository.save(alert);
    }

    private List<HealthRecord> findRecentThreeDayRecords(HealthRecord healthRecord) {
        List<HealthRecord> records = healthRecordRepository.findByPet_IdOrderByDateAsc(healthRecord.getPet().getId());
        List<HealthRecord> recentRecords = new ArrayList<>();
        Date baseDate = healthRecord.getDate() == null ? new Date() : healthRecord.getDate();
        long cutoffTime = baseDate.getTime() - 3L * 24L * 60L * 60L * 1000L;

        for (HealthRecord record : records) {
            if (record.getDate() != null && record.getDate().getTime() >= cutoffTime) {
                recentRecords.add(record);
            }
        }

        if (recentRecords.size() <= 3) {
            return recentRecords;
        }
        return new ArrayList<>(recentRecords.subList(recentRecords.size() - 3, recentRecords.size()));
    }

    private void createAlert(HealthRecord healthRecord, String message, String severity) {
        if (healthRecord.getPet() == null || healthRecord.getPet().getId() == null) {
            return;
        }

        HealthAlert latestSameAlert = healthAlertRepository
                .findFirstByPet_IdAndMessageOrderByDateDesc(healthRecord.getPet().getId(), message)
                .orElse(null);
        if (latestSameAlert != null && Boolean.FALSE.equals(latestSameAlert.getHandled())) {
            return;
        }

        HealthAlert alert = new HealthAlert();
        alert.setPet(healthRecord.getPet());
        alert.setMessage(message);
        alert.setDate(new Date());
        alert.setSeverity(severity);
        alert.setHandled(false);
        healthAlertRepository.save(alert);
    }

    private boolean hasPositiveWeight(HealthRecord healthRecord) {
        return healthRecord.getWeight() != null && healthRecord.getWeight() > 0;
    }

    private boolean hasFoodIntake(HealthRecord healthRecord) {
        return healthRecord.getFoodIntake() != null && healthRecord.getFoodIntake() > 0;
    }

    private boolean hasWaterIntake(HealthRecord healthRecord) {
        return healthRecord.getWaterIntake() != null && healthRecord.getWaterIntake() > 0;
    }
}
