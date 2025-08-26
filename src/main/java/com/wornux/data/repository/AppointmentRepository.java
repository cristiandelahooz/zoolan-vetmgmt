package com.wornux.data.repository;

import com.wornux.data.entity.Appointment;
import java.time.LocalDateTime;
import java.util.List;
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

  @Query(
      "SELECT a FROM Appointment a WHERE a.startAppointmentDate >= :startOfDay AND a.startAppointmentDate < :endOfDay")
  List<Appointment> findTodayAppointments(
      @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

  @Query(
      "SELECT a FROM Appointment a WHERE a.startAppointmentDate BETWEEN :now AND :tomorrow AND"
          + " a.status NOT IN ('CANCELADA', 'COMPLETADA') ORDER BY a.startAppointmentDate")
  List<Appointment> findUpcomingAppointments(
      @Param("now") LocalDateTime now, @Param("tomorrow") LocalDateTime tomorrow);
}
