package com.wornux.data.repository;

import com.wornux.data.entity.WaitingRoom;
import com.wornux.data.enums.VisitType;
import com.wornux.data.enums.WaitingRoomStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitingRoomRepository
    extends JpaRepository<WaitingRoom, Long>, JpaSpecificationExecutor<WaitingRoom> {

  @Query(
      "SELECT wr FROM WaitingRoom wr "
          + "JOIN FETCH wr.client c "
          + "JOIN FETCH wr.pet p "
          + "WHERE wr.status IN (:statuses) "
          + "ORDER BY wr.priority DESC, wr.arrivalTime ASC")
  List<WaitingRoom> findCurrentWaitingRoom(@Param("statuses") List<WaitingRoomStatus> statuses);

  @Query(
      "SELECT wr FROM WaitingRoom wr "
          + "JOIN FETCH wr.client c "
          + "JOIN FETCH wr.pet p "
          + "WHERE wr.status = 'WAITING' "
          + "ORDER BY wr.priority DESC, wr.arrivalTime ASC")
  List<WaitingRoom> findWaitingEntries();

  @Query(
      "SELECT wr FROM WaitingRoom wr "
          + "JOIN FETCH wr.client c "
          + "JOIN FETCH wr.pet p "
          + "WHERE wr.status = 'IN_CONSULTATION' "
          + "ORDER BY wr.consultationStartedAt ASC")
  List<WaitingRoom> findInConsultationEntries();

  long countByStatus(WaitingRoomStatus status);

  @Query(
      "SELECT wr FROM WaitingRoom wr "
          + "WHERE wr.client.id = :clientId "
          + "AND wr.pet.id = :petId "
          + "AND wr.status = 'ESPERANDO'")
  List<WaitingRoom> findWaitingByClientAndPet(
      @Param("clientId") Long clientId, @Param("petId") Long petId);

  @Query(
      "SELECT wr FROM WaitingRoom wr "
          + "JOIN FETCH wr.client c "
          + "JOIN FETCH wr.pet p "
          + "WHERE wr.arrivalTime >= :startOfDay AND wr.arrivalTime < :endOfDay "
          + "ORDER BY wr.arrivalTime DESC")
  Page<WaitingRoom> findTodayHistory(
      @Param("startOfDay") LocalDateTime startOfDay,
      @Param("endOfDay") LocalDateTime endOfDay,
      Pageable pageable);

  @Query(
      "SELECT wr FROM WaitingRoom wr "
          + "WHERE wr.arrivalTime >= :startOfDay AND wr.arrivalTime < :endOfDay "
          + "ORDER BY wr.arrivalTime DESC")
  Page<WaitingRoom> findTodayHistorySimple(
      @Param("startOfDay") LocalDateTime startOfDay,
      @Param("endOfDay") LocalDateTime endOfDay,
      Pageable pageable);

  List<WaitingRoom> findByStatusOrderByPriorityAscArrivalTimeAsc(WaitingRoomStatus status);

  Optional<WaitingRoom> findTopByPet_IdAndStatusInOrderByArrivalTimeDesc(
      Long petId, Collection<WaitingRoomStatus> statuses);

  List<WaitingRoom> findByAssignedVeterinarian_Id(Long veterinarianId);

  @Query(
      "SELECT wr FROM WaitingRoom wr "
          + "JOIN FETCH wr.client c "
          + "JOIN FETCH wr.pet p "
          + "WHERE wr.assignedVeterinarian.id = :veterinarianId "
          + "AND wr.status IN (:statuses) "
          + "ORDER BY wr.priority DESC, wr.arrivalTime ASC")
  List<WaitingRoom> findByVeterinarianAndStatuses(
      @Param("veterinarianId") Long veterinarianId,
      @Param("statuses") List<WaitingRoomStatus> statuses);

  List<WaitingRoom> findByAssignedGroomer_Id(Long groomerId);

  @Query(
      """
    SELECT wr FROM WaitingRoom wr
      JOIN FETCH wr.client c
      JOIN FETCH wr.pet p
    WHERE wr.assignedGroomer.id = :groomerId
      AND wr.status IN (:statuses)
    ORDER BY wr.priority DESC, wr.arrivalTime ASC
""")
  List<WaitingRoom> findByGroomerAndStatuses(
      @Param("groomerId") Long groomerId, @Param("statuses") List<WaitingRoomStatus> statuses);


//------------------------------------------------------------------
  boolean existsByPet_IdAndStatusInAndArrivalTimeBetween(
          Long petId,
          java.util.Collection<com.wornux.data.enums.WaitingRoomStatus> statuses,
          java.time.LocalDateTime startInclusive,
          java.time.LocalDateTime endExclusive);

  @Query("""
  SELECT wr FROM WaitingRoom wr
    JOIN FETCH wr.client c
    JOIN FETCH wr.pet p
  WHERE wr.type = :visitType
    AND wr.status IN (:statuses)
    AND (wr.assignedVeterinarian IS NULL OR wr.assignedVeterinarian.id = :vetId)
  ORDER BY wr.priority DESC, wr.arrivalTime ASC
""")
  List<WaitingRoom> findActiveMedicalForVetOrUnassigned(
          @Param("vetId") Long veterinarianId,
          @Param("visitType") com.wornux.data.enums.VisitType visitType,
          @Param("statuses") List<com.wornux.data.enums.WaitingRoomStatus> statuses);

  // GROOMER: traer GROOMING activas donde el groomer sea el actual O esté sin asignar
  @Query("""
  SELECT wr FROM WaitingRoom wr
    JOIN FETCH wr.client c
    JOIN FETCH wr.pet p
  WHERE wr.type = :visitType
    AND wr.status IN (:statuses)
    AND (wr.assignedGroomer IS NULL OR wr.assignedGroomer.id = :groomerId)
  ORDER BY wr.priority DESC, wr.arrivalTime ASC
""")
  List<WaitingRoom> findActiveGroomingForGroomerOrUnassigned(
          @Param("groomerId") Long groomerId,
          @Param("visitType") com.wornux.data.enums.VisitType visitType,
          @Param("statuses") List<com.wornux.data.enums.WaitingRoomStatus> statuses);

  // evitar duplicados exactos por (mascota, hora de cita, tipo)
  boolean existsByPet_IdAndArrivalTimeAndType(
          Long petId,
          java.time.LocalDateTime arrivalTime,
          com.wornux.data.enums.VisitType type);

  // (opcional) versión “tolerante” por si la hora pudiera variar unos minutos
  boolean existsByPet_IdAndTypeAndArrivalTimeBetween(
          Long petId,
          com.wornux.data.enums.VisitType type,
          java.time.LocalDateTime from,
          java.time.LocalDateTime to);




}
