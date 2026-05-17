package com.petfeeding.system.controller;

import com.petfeeding.system.model.FeedingPlan;
import com.petfeeding.system.service.FeedingPlanService;
import com.petfeeding.system.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feeding-plan")
public class FeedingPlanController {
    @Autowired
    private FeedingPlanService feedingPlanService;

    @Autowired
    private PetService petService;

    @GetMapping("/{petId}")
    public ResponseEntity<?> getFeedingPlan(@PathVariable Long petId) {
        // 生成或获取喂养方案
        FeedingPlan feedingPlan = feedingPlanService.generateFeedingPlan(petService.findById(petId));
        return ResponseEntity.ok(feedingPlan);
    }
}