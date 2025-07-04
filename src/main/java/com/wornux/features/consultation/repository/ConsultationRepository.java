package com.wornux.features.consultation.repository;

import com.zoolandia.app.common.entity.AbstractRepository;
import com.wornux.features.consultation.domain.Consultation;
import com.zoolandia.app.features.employee.domain.Employee;
import com.zoolandia.app.features.pet.domain.Pet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRepository extends AbstractRepository<Consultation, Long>, JpaSpecificationExecutor<Consultation> {
    Optional<Consultation> findByPet(Pet pet);

    Optional<Consultation> findByVeterinarian(Employee veterinarian);

    Page<Consultation> findByActiveTrue(Pageable pageable);
    List<Consultation> findByPetIdAndActiveTrue(Long petId);
    List<Consultation> findByVeterinarianIdAndActiveTrue(Long veterinarianId);
}
