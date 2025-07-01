package com.zoolandia.app.features.medicalHistory.repository;

import com.zoolandia.app.common.entity.AbstractRepository;
import com.zoolandia.app.features.medicalHistory.domain.MedicalHistory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalHistoryRepository extends AbstractRepository<MedicalHistory, Long> {
    Optional<MedicalHistory> findByPetId(Long petId);
    MedicalHistory save(MedicalHistory medicalHistory);
}
