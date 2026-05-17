package com.petfeeding.system.service;

import com.petfeeding.system.model.FeedingPlan;
import com.petfeeding.system.model.Pet;
import com.petfeeding.system.repository.FeedingPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedingPlanService {
    @Autowired
    private FeedingPlanRepository feedingPlanRepository;

    public FeedingPlan generateFeedingPlan(Pet pet) {
        // 计算静息能量需求 (RER)
        double rer = calculateRER(pet.getWeight());
        
        // 计算每日能量需求 (DER)，根据活动量调整
        double der = calculateDER(rer, pet.getActivityLevel());
        
        // 计算食物配比
        int protein = calculateProteinRatio(pet);
        int fat = calculateFatRatio(pet);
        int carbs = 100 - protein - fat;
        
        // 计算喂食频次
        int frequency = calculateFeedingFrequency(pet.getAge());
        
        // 计算每次食量（假设食物热量为3.5 kcal/g）
        double portionSize = der / frequency / 3.5;
        
        // 创建或更新喂养方案
        FeedingPlan feedingPlan = feedingPlanRepository.findByPetId(pet.getId())
                .orElse(new FeedingPlan());
        
        feedingPlan.setPet(pet);
        feedingPlan.setDailyEnergy(der);
        feedingPlan.setProtein(protein);
        feedingPlan.setFat(fat);
        feedingPlan.setCarbs(carbs);
        feedingPlan.setFrequency(frequency);
        feedingPlan.setPortionSize(portionSize);
        
        return feedingPlanRepository.save(feedingPlan);
    }

    // 计算静息能量需求 (RER)
    private double calculateRER(double weight) {
        // 使用公式: RER = 70 * (体重kg)^0.75
        return 70 * Math.pow(weight, 0.75);
    }

    // 计算每日能量需求 (DER)
    private double calculateDER(double rer, String activityLevel) {
        switch (activityLevel.toLowerCase()) {
            case "low":
                return rer * 1.2;
            case "medium":
                return rer * 1.5;
            case "high":
                return rer * 2.0;
            default:
                return rer * 1.5;
        }
    }

    // 计算蛋白质比例
    private int calculateProteinRatio(Pet pet) {
        // 根据宠物年龄和品种调整蛋白质比例
        if (pet.getAge() < 1) {
            // 幼年期需要更多蛋白质
            return 35;
        } else if (pet.getAge() > 7) {
            // 老年期需要适量蛋白质
            return 25;
        } else {
            // 成年期标准蛋白质比例
            return 30;
        }
    }

    // 计算脂肪比例
    private int calculateFatRatio(Pet pet) {
        // 根据宠物年龄和品种调整脂肪比例
        if (pet.getAge() < 1) {
            // 幼年期需要更多脂肪
            return 25;
        } else if (pet.getAge() > 7) {
            // 老年期需要适量脂肪
            return 20;
        } else {
            // 成年期标准脂肪比例
            return 22;
        }
    }

    // 计算喂食频次
    private int calculateFeedingFrequency(int age) {
        if (age < 1) {
            // 幼年期需要多次喂食
            return 4;
        } else if (age > 7) {
            // 老年期需要适量喂食
            return 3;
        } else {
            // 成年期标准喂食频次
            return 2;
        }
    }

    public FeedingPlan findByPetId(Long petId) {
        return feedingPlanRepository.findByPetId(petId).orElse(null);
    }
}