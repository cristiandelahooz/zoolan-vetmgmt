package com.zoolandia.app.features.medicalHistory.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.vaadin.hilla.crud.ListRepositoryService;
import com.zoolandia.app.features.medicalHistory.domain.MedicalHistory;
import com.zoolandia.app.features.medicalHistory.repository.MedicalHistoryRepository;
import com.zoolandia.app.features.pet.domain.Pet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Medical History not found for Pet with id: " + petId));
    }

}
