package com.wornux.features.medicalHistory.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.wornux.features.medicalHistory.domain.MedicalHistory;
import com.wornux.features.medicalHistory.repository.MedicalHistoryRepository;
import com.wornux.features.pet.domain.Pet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
public class MedicalHistoryServiceImpl extends ListRepositoryService<MedicalHistory, Long, MedicalHistoryRepository> implements  MedicalHistoryService {
    private final MedicalHistoryRepository medicalHistoryRepository;

    @Override
    public MedicalHistory findOrCreateByPet(Pet pet) {
        return medicalHistoryRepository.findByPetId(pet.getId())
                .orElseGet(() -> {
                    MedicalHistory newHistory = new MedicalHistory();
                    newHistory.setPet(pet);
                    return medicalHistoryRepository.save(newHistory);
                });
    }

    @Override
    public MedicalHistory findByPetId(Long petId) {
        return medicalHistoryRepository.findByPetId(petId)
                .orElseGet(() -> {
                    MedicalHistory newHistory = new MedicalHistory();
                    Pet pet = new Pet();
                    pet.setId(petId);
                    newHistory.setPet(pet);
                    return newHistory;
                });
    }

    @Override
    public MedicalHistory getOrCreateMedicalHistory(Pet pet) {
        return findOrCreateByPet(pet);
    }

    @Override
    public MedicalHistory updateMedicalHistory(MedicalHistory medicalHistory) {
        return medicalHistoryRepository.save(medicalHistory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalHistory> getConsultationsByPetId(Long petId) {
        log.debug("Request to get medical history with consultations for Pet: {}", petId);

        Optional<MedicalHistory> medicalHistory = medicalHistoryRepository.findByPetId(petId);
        if (medicalHistory.isPresent()) {
            // Force load consultations
            MedicalHistory history = medicalHistory.get();
            history.getConsultations().size(); // Force lazy loading
            return List.of(history);
        }
        return List.of();
    }

}
