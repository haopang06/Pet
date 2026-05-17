package com.petfeeding.system.controller;

import com.petfeeding.system.model.HealthRecord;
import com.petfeeding.system.service.HealthRecordService;
import com.petfeeding.system.service.HealthAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {
    @Autowired
    private HealthRecordService healthRecordService;

    @Autowired
    private HealthAlertService healthAlertService;

    @PostMapping
    public ResponseEntity<?> createHealthRecord(@RequestBody HealthRecord healthRecord) {
        HealthRecord savedRecord = healthRecordService.save(healthRecord);
        return ResponseEntity.ok(savedRecord);
    }

    @GetMapping("/{petId}")
    public ResponseEntity<?> getHealthRecords(@PathVariable Long petId) {
        List<HealthRecord> records = healthRecordService.findByPetId(petId);
        return ResponseEntity.ok(records);
    }
}

@RestController
@RequestMapping("/api/health-alerts")
class HealthAlertController {
    @Autowired
    private HealthAlertService healthAlertService;

    @GetMapping("/{petId}")
    public ResponseEntity<?> getHealthAlerts(@PathVariable Long petId) {
        return ResponseEntity.ok(healthAlertService.findByPetId(petId));
    }
}