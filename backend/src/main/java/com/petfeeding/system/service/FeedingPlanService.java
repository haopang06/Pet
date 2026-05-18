package com.petfeeding.system.service;

import com.petfeeding.system.model.FeedingPlan;
import com.petfeeding.system.model.Pet;
import com.petfeeding.system.repository.FeedingPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class FeedingPlanService {
    @Autowired
    private FeedingPlanRepository feedingPlanRepository;

    private static final List<String> LARGE_DOG_BREEDS = Arrays.asList("金毛", "哈士奇", "萨摩耶", "拉布拉多", "阿拉斯加", "边牧", "德牧");
    private static final List<String> ACTIVE_CAT_BREEDS = Arrays.asList("美短", "暹罗", "孟加拉", "缅因");
    private static final List<String> LOW_ACTIVITY_CAT_BREEDS = Arrays.asList("英短", "金渐层", "银渐层", "加菲", "波斯");

    public FeedingPlan generateFeedingPlan(Pet pet) {
        // 计算静息能量需求 (RER)
        double rer = calculateRER(pet.getWeight());
        
        // 计算每日能量需求 (DER)，根据宠物类型、品种和活动量调整
        double der = calculateDER(rer, pet);
        
        // 计算食物配比
        int protein = calculateProteinRatio(pet);
        int fat = calculateFatRatio(pet);
        int carbs = 100 - protein - fat;
        
        // 计算喂食频次
        int frequency = calculateFeedingFrequency(pet.getAge());
        
        // 按常见湿粮/罐头和冻干热量密度分别估算单次喂食量
        double cannedPortionSize = der / frequency / calculateCannedCaloriesPerGram(pet);
        double freezeDriedPortionSize = der / frequency / calculateFreezeDriedCaloriesPerGram(pet);
        
        // 创建或更新喂养方案
        FeedingPlan feedingPlan = feedingPlanRepository.findByPetId(pet.getId())
                .orElse(new FeedingPlan());
        
        feedingPlan.setPet(pet);
        feedingPlan.setDailyEnergy(round(der));
        feedingPlan.setProtein(protein);
        feedingPlan.setFat(fat);
        feedingPlan.setCarbs(carbs);
        feedingPlan.setFrequency(frequency);
        feedingPlan.setPortionSize(round(cannedPortionSize));
        feedingPlan.setCannedPortionSize(round(cannedPortionSize));
        feedingPlan.setFreezeDriedPortionSize(round(freezeDriedPortionSize));
        
        return feedingPlanRepository.save(feedingPlan);
    }

    // 计算静息能量需求 (RER)
    private double calculateRER(double weight) {
        // 使用公式: RER = 70 * (体重kg)^0.75
        return 70 * Math.pow(weight, 0.75);
    }

    // 计算每日能量需求 (DER)
    private double calculateDER(double rer, Pet pet) {
        double factor = calculateActivityFactor(pet);
        return rer * factor * calculateBreedFactor(pet);
    }

    private double calculateActivityFactor(Pet pet) {
        String petType = normalize(pet.getPetType());
        String activityLevel = normalize(pet.getActivityLevel());

        if ("cat".equals(petType)) {
            switch (activityLevel) {
                case "low":
                    return 1.0;
                case "high":
                    return 1.4;
                default:
                    return 1.2;
            }
        }

        switch (activityLevel) {
            case "low":
                return 1.2;
            case "high":
                return 2.0;
            default:
                return 1.6;
        }
    }

    private double calculateBreedFactor(Pet pet) {
        String petType = normalize(pet.getPetType());
        String breed = normalize(pet.getBreed());

        if ("dog".equals(petType) && LARGE_DOG_BREEDS.contains(breed)) {
            return 1.08;
        }
        if ("cat".equals(petType) && ACTIVE_CAT_BREEDS.contains(breed)) {
            return 1.05;
        }
        if ("cat".equals(petType) && LOW_ACTIVITY_CAT_BREEDS.contains(breed)) {
            return 0.95;
        }
        return 1.0;
    }

    // 计算蛋白质比例
    private int calculateProteinRatio(Pet pet) {
        String petType = normalize(pet.getPetType());

        if (pet.getAge() < 1) {
            return "cat".equals(petType) ? 42 : 35;
        } else if (pet.getAge() > 7) {
            return "cat".equals(petType) ? 34 : 25;
        }
        return "cat".equals(petType) ? 38 : 30;
    }

    // 计算脂肪比例
    private int calculateFatRatio(Pet pet) {
        String petType = normalize(pet.getPetType());

        if (pet.getAge() < 1) {
            return "cat".equals(petType) ? 24 : 25;
        } else if (pet.getAge() > 7) {
            return "cat".equals(petType) ? 18 : 20;
        }
        return "cat".equals(petType) ? 20 : 22;
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

    private double calculateCannedCaloriesPerGram(Pet pet) {
        return "cat".equals(normalize(pet.getPetType())) ? 0.9 : 1.0;
    }

    private double calculateFreezeDriedCaloriesPerGram(Pet pet) {
        return "cat".equals(normalize(pet.getPetType())) ? 4.2 : 4.5;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public FeedingPlan findByPetId(Long petId) {
        return feedingPlanRepository.findByPetId(petId).orElse(null);
    }
}
