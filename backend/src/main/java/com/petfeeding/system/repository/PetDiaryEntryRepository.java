package com.petfeeding.system.repository;

import com.petfeeding.system.model.PetDiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PetDiaryEntryRepository extends JpaRepository<PetDiaryEntry, Long> {
    List<PetDiaryEntry> findByPet_IdOrderByDiaryDateDesc(Long petId);

    Optional<PetDiaryEntry> findFirstByPet_IdAndDiaryDate(Long petId, Date diaryDate);

    void deleteByPet_Id(Long petId);
}
