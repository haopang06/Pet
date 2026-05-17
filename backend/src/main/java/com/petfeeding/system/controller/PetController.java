package com.petfeeding.system.controller;

import com.petfeeding.system.model.Pet;
import com.petfeeding.system.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    @Autowired
    private PetService petService;

    @PostMapping
    public ResponseEntity<?> createPet(@RequestBody Pet pet) {
        Pet savedPet = petService.save(pet);
        return ResponseEntity.ok(savedPet);
    }

    @GetMapping
    public ResponseEntity<?> getPets() {
        // 这里应该根据当前用户获取宠物列表
        // 暂时返回所有宠物
        List<Pet> pets = petService.findByUserId(1L);
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPet(@PathVariable Long id) {
        Pet pet = petService.findById(id);
        return ResponseEntity.ok(pet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePet(@PathVariable Long id, @RequestBody Pet pet) {
        pet.setId(id);
        Pet updatedPet = petService.save(pet);
        return ResponseEntity.ok(updatedPet);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePet(@PathVariable Long id) {
        petService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}