package com.wornux.data.repository;

import com.wornux.data.base.AbstractRepository;
import com.wornux.data.entity.MedicalHistory;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalHistoryRepository extends AbstractRepository<MedicalHistory, Long> {
    Optional<MedicalHistory> findByPetId(Long petId);
}
