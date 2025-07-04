package com.wornux.repository;

import com.wornux.common.entity.AbstractRepository;
import com.wornux.domain.MedicalHistory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalHistoryRepository extends AbstractRepository<MedicalHistory, Long> {
    Optional<MedicalHistory> findByPetId(Long petId);

    MedicalHistory save(MedicalHistory medicalHistory);
}
