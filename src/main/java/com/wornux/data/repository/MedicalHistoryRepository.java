package com.wornux.data.repository;

import com.wornux.data.base.AbstractRepository;
import com.wornux.data.entity.MedicalHistory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicalHistoryRepository extends AbstractRepository<MedicalHistory, Long> {
    Optional<MedicalHistory> findByPetId(Long petId);
}