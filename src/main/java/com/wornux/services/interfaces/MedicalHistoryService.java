package com.wornux.services.interfaces;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.MedicalHistory;
import com.wornux.data.entity.Pet;
import java.util.List;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

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
