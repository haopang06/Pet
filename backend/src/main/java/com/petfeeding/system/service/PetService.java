package com.petfeeding.system.service;

import com.petfeeding.system.model.Pet;
import com.petfeeding.system.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetService {
    @Autowired
    private PetRepository petRepository;

    public Pet save(Pet pet) {
        return petRepository.save(pet);
    }

    public List<Pet> findByUserId(Long userId) {
        return petRepository.findByUserId(userId);
    }

    public Pet findById(Long id) {
        return petRepository.findById(id).orElse(null);
    }

    public void deleteById(Long id) {
        petRepository.deleteById(id);
    }
}