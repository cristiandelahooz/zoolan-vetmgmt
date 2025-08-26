package com.wornux.data.repository;

import com.wornux.data.base.AbstractRepository;
import com.wornux.data.entity.Consultation;
import com.wornux.data.entity.Employee;
import com.wornux.data.entity.Pet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultationRepository
    extends AbstractRepository<Consultation, Long>, JpaSpecificationExecutor<Consultation> {
  Optional<Consultation> findByPet(Pet pet);

  Optional<Consultation> findByVeterinarian(Employee veterinarian);

  Page<Consultation> findByActiveTrue(Pageable pageable);

  List<Consultation> findByPetIdAndActiveTrue(Long petId);

  List<Consultation> findByVeterinarianIdAndActiveTrue(Long veterinarianId);

  @Query(
      "SELECT DATE(c.consultationDate), COUNT(c) "
          + "FROM Consultation c WHERE c.consultationDate >= :startDate AND c.consultationDate <= :endDate "
          + "AND c.active = true GROUP BY DATE(c.consultationDate) ORDER BY DATE(c.consultationDate)")
  List<Object[]> findDailyConsultationCounts(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query(
      "SELECT HOUR(c.consultationDate), COUNT(c) "
          + "FROM Consultation c WHERE c.consultationDate >= :startDate AND c.consultationDate <= :endDate "
          + "AND c.active = true GROUP BY HOUR(c.consultationDate) ORDER BY HOUR(c.consultationDate)")
  List<Object[]> findEmployeeUtilizationByHour(
      @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  List<Consultation> findByVeterinarian_IdAndActiveTrueOrderByConsultationDateDesc(
      Long veterinarianId);
}
