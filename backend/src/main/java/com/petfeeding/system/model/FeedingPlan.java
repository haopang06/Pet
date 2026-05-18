package com.petfeeding.system.model;

import javax.persistence.*;

@Entity
@Table(name = "feeding_plans")
public class FeedingPlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double dailyEnergy;
    private Integer protein;
    private Integer fat;
    private Integer carbs;
    private Integer frequency;
    private Double portionSize;
    private Double cannedPortionSize;
    private Double freezeDriedPortionSize;

    @ManyToOne
    @JoinColumn(name = "pet_id")
    private Pet pet;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getDailyEnergy() {
        return dailyEnergy;
    }

    public void setDailyEnergy(Double dailyEnergy) {
        this.dailyEnergy = dailyEnergy;
    }

    public Integer getProtein() {
        return protein;
    }

    public void setProtein(Integer protein) {
        this.protein = protein;
    }

    public Integer getFat() {
        return fat;
    }

    public void setFat(Integer fat) {
        this.fat = fat;
    }

    public Integer getCarbs() {
        return carbs;
    }

    public void setCarbs(Integer carbs) {
        this.carbs = carbs;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Double getPortionSize() {
        return portionSize;
    }

    public void setPortionSize(Double portionSize) {
        this.portionSize = portionSize;
    }

    public Double getCannedPortionSize() {
        return cannedPortionSize;
    }

    public void setCannedPortionSize(Double cannedPortionSize) {
        this.cannedPortionSize = cannedPortionSize;
    }

    public Double getFreezeDriedPortionSize() {
        return freezeDriedPortionSize;
    }

    public void setFreezeDriedPortionSize(Double freezeDriedPortionSize) {
        this.freezeDriedPortionSize = freezeDriedPortionSize;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }
}
