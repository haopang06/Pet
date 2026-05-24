package com.petfeeding.system.service;

import com.petfeeding.system.model.Pet;
import com.petfeeding.system.model.PetDiaryEntry;
import com.petfeeding.system.repository.PetDiaryEntryRepository;
import com.petfeeding.system.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PetDiaryEntryService {
    @Autowired
    private PetDiaryEntryRepository petDiaryEntryRepository;

    @Autowired
    private PetRepository petRepository;

    public List<PetDiaryEntry> findByPetId(Long petId) {
        return petDiaryEntryRepository.findByPet_IdOrderByDiaryDateDesc(petId);
    }

    public PetDiaryEntry save(PetDiaryEntry entry) {
        Long petId = resolvePetId(entry);
        if (petId == null) {
            throw new IllegalArgumentException("请选择宠物");
        }
        Date diaryDate = entry.getDiaryDate();
        if (diaryDate == null) {
            throw new IllegalArgumentException("请选择日期");
        }

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new IllegalArgumentException("宠物不存在"));

        PetDiaryEntry target = petDiaryEntryRepository
                .findFirstByPet_IdAndDiaryDate(petId, diaryDate)
                .orElseGet(PetDiaryEntry::new);

        target.setPet(pet);
        target.setDiaryDate(diaryDate);
        target.setContent(entry.getContent() == null ? "" : entry.getContent().trim());
        target.setImages(entry.getImages() == null ? "[]" : entry.getImages());
        if (entry.getFavorite() != null) {
            target.setFavorite(entry.getFavorite());
        }
        return petDiaryEntryRepository.save(target);
    }

    public void deleteByPetId(Long petId) {
        petDiaryEntryRepository.deleteByPet_Id(petId);
    }

    private Long resolvePetId(PetDiaryEntry entry) {
        if (entry.getPetId() != null) {
            return entry.getPetId();
        }
        Pet pet = entry.getPet();
        return pet != null ? pet.getId() : null;
    }
}
