package com.wornux.features.medicalHistory.service;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.features.medicalHistory.domain.MedicalHistory;
import com.wornux.features.pet.domain.Pet;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@BrowserCallable
@Validated
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface MedicalHistoryService {
    MedicalHistory findOrCreateByPet(Pet pet);

    MedicalHistory findByPetId(Long petId);

    MedicalHistory getOrCreateMedicalHistory(Pet pet);

    MedicalHistory updateMedicalHistory(MedicalHistory medicalHistory);

    List<MedicalHistory> getConsultationsByPetId(Long petId);
}
