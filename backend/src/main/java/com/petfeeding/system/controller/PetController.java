package com.petfeeding.system.controller;

import com.petfeeding.system.model.Pet;
import com.petfeeding.system.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pets")
public class PetController {
    @Autowired
    private PetService petService;

    @PostMapping
    public ResponseEntity<?> createPet(@RequestBody Pet pet,
                                       @RequestParam(value = "userId", required = false) Long userId) {
        try {
            Pet savedPet = petService.saveForUser(pet, resolveUserId(pet, userId));
            return ResponseEntity.ok(savedPet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getPets(@RequestParam(value = "userId", required = false) Long userId) {
        List<Pet> pets = userId == null ? petService.findAll() : petService.findByUserId(userId);
        return ResponseEntity.ok(pets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPet(@PathVariable Long id) {
        Pet pet = petService.findById(id);
        return ResponseEntity.ok(pet);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePet(@PathVariable Long id,
                                       @RequestBody Pet pet,
                                       @RequestParam(value = "userId", required = false) Long userId) {
        try {
            pet.setId(id);
            Pet updatedPet = petService.saveForUser(pet, resolveUserId(pet, userId));
            return ResponseEntity.ok(updatedPet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePet(@PathVariable Long id) {
        petService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/{id}/note", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<?> updatePetNote(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            Pet updatedPet = petService.updateNote(id, payload.get("note"), payload.get("noteImages"));
            return ResponseEntity.ok(updatedPet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    @RequestMapping(value = "/{id}/photo", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<?> updatePetPhoto(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            Pet updatedPet = petService.updatePhoto(id, payload.getOrDefault("photo", ""));
            return ResponseEntity.ok(updatedPet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }

    private Long resolveUserId(Pet pet, Long userId) {
        if (userId != null) {
            return userId;
        }
        if (pet != null && pet.getUser() != null && pet.getUser().getId() != null) {
            return pet.getUser().getId();
        }
        return null;
    }
}
