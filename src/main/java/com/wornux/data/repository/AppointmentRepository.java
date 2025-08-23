package com.wornux.data.repository;

import com.wornux.data.entity.Appointment;
import com.wornux.data.enums.AppointmentStatus;
import com.wornux.data.enums.OfferingType;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository
    extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

  List<Appointment> findByStartAppointmentDateBetween(LocalDateTime start, LocalDateTime end);

  @Query("SELECT a FROM Appointment a WHERE DATE(a.startAppointmentDate) = DATE(:date)")
  List<Appointment> findByAppointmentDate(@Param("date") LocalDateTime date);

  List<Appointment> findByClientIdOrderByStartAppointmentDateDesc(Long clientId);

  List<Appointment> findByPetIdOrderByStartAppointmentDateDesc(Long petId);

  List<Appointment> findByAssignedEmployeeIdAndStartAppointmentDateBetween(
      Long employeeId, LocalDateTime start, LocalDateTime end);

  Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

  List<Appointment> findByServiceType(OfferingType serviceType);

  @Query(
      "SELECT a FROM Appointment a WHERE a.startAppointmentDate >= :startOfDay AND a.startAppointmentDate < :endOfDay")
  List<Appointment> findTodayAppointments(
      @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

  @Query(
      "SELECT a FROM Appointment a WHERE a.startAppointmentDate BETWEEN :now AND :tomorrow AND"
          + " a.status NOT IN ('CANCELADA', 'COMPLETADA') ORDER BY a.startAppointmentDate")
  List<Appointment> findUpcomingAppointments(
      @Param("now") LocalDateTime now, @Param("tomorrow") LocalDateTime tomorrow);

  @Query(
      "SELECT a FROM Appointment a WHERE a.assignedEmployee.id = :employeeId "
          + "AND a.status NOT IN ('CANCELADA') "
          + "AND a.endAppointmentDate < :endTime "
          + "AND a.startAppointmentDate >= :searchStart")
  List<Appointment> findConflictingAppointments(
      @Param("employeeId") Long employeeId,
      @Param("searchStart") LocalDateTime searchStart,
      @Param("endTime") LocalDateTime endTime);

  @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
  Long countByStatus(@Param("status") AppointmentStatus status);

  @Query("SELECT a.serviceType, COUNT(a) FROM Appointment a GROUP BY a.serviceType")
  List<Object[]> getAppointmentCountByServiceType();
}
