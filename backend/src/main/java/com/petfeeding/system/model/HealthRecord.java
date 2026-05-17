package com.petfeeding.system.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "health_records")
public class HealthRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double weight;
    private String mentalState;
    private String defecation;
    private Date date;

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

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public String getMentalState() {
        return mentalState;
    }

    public void setMentalState(String mentalState) {
        this.mentalState = mentalState;
    }

    public String getDefecation() {
        return defecation;
    }

    public void setDefecation(String defecation) {
        this.defecation = defecation;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }
}