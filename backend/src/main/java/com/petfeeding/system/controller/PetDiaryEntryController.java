package com.petfeeding.system.controller;

import com.petfeeding.system.model.PetDiaryEntry;
import com.petfeeding.system.service.PetDiaryEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/pet-diaries")
public class PetDiaryEntryController {
    @Autowired
    private PetDiaryEntryService petDiaryEntryService;

    @GetMapping("/{petId}")
    public ResponseEntity<?> getDiaries(@PathVariable Long petId) {
        List<PetDiaryEntry> diaryEntries = petDiaryEntryService.findByPetId(petId);
        return ResponseEntity.ok(diaryEntries);
    }

    @PostMapping
    public ResponseEntity<?> saveDiary(@RequestBody PetDiaryEntry diaryEntry) {
        try {
            PetDiaryEntry savedEntry = petDiaryEntryService.save(diaryEntry);
            return ResponseEntity.ok(savedEntry);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("message", e.getMessage()));
        }
    }
}
