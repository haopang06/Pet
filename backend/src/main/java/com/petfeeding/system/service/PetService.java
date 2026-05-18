package com.petfeeding.system.service;

import com.petfeeding.system.model.Pet;
import com.petfeeding.system.model.User;
import com.petfeeding.system.repository.FeedingPlanRepository;
import com.petfeeding.system.repository.PetRepository;
import com.petfeeding.system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetService {
    @Autowired
    private PetRepository petRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FeedingPlanRepository feedingPlanRepository;

    public Pet save(Pet pet) {
        return petRepository.save(pet);
    }

    public Pet saveForUser(Pet pet, Long userId) {
        if (pet.getId() != null) {
            petRepository.findById(pet.getId()).ifPresent(existingPet -> {
                if (pet.getNote() == null) {
                    pet.setNote(existingPet.getNote());
                }
            });
        }
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
            pet.setUser(user);
        }
        return petRepository.save(pet);
    }

    public List<Pet> findAll() {
        return petRepository.findAll();
    }

    public List<Pet> findByUserId(Long userId) {
        return petRepository.findByUserId(userId);
    }

    public Pet findById(Long id) {
        return petRepository.findById(id).orElse(null);
    }

    public Pet updateNote(Long id, String note) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("宠物不存在"));
        pet.setNote(note);
        return petRepository.save(pet);
    }

    public void deleteById(Long id) {
        feedingPlanRepository.findByPetId(id).ifPresent(feedingPlanRepository::delete);
        petRepository.deleteById(id);
    }
}
