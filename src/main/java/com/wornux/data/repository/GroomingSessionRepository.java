package com.wornux.data.repository;

import com.wornux.data.base.AbstractRepository;
import com.wornux.data.entity.Employee;
import com.wornux.data.entity.GroomingSession;
import com.wornux.data.entity.Pet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GroomingSessionRepository
    extends AbstractRepository<GroomingSession, Long>, JpaSpecificationExecutor<GroomingSession> {

  Optional<GroomingSession> findByPet(Pet pet);

  Optional<GroomingSession> findByGroomer(Employee groomer);

  Page<GroomingSession> findByActiveTrue(Pageable pageable);

  List<GroomingSession> findByPetIdAndActiveTrue(Long petId);

  List<GroomingSession> findByGroomerIdAndActiveTrue(Long groomerId);
}
