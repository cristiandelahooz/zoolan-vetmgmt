package com.zoolandia.app.features.appointments.repository;

import com.zoolandia.app.features.appointments.domain.Appointment;
import com.zoolandia.app.features.appointments.domain.AppointmentStatus;
import com.zoolandia.app.features.appointments.domain.ServiceType;
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

  List<Appointment> findByAppointmentDateTimeBetween(LocalDateTime start, LocalDateTime end);

  @Query("SELECT a FROM Appointment a WHERE DATE(a.appointmentDateTime) = DATE(:date)")
  List<Appointment> findByAppointmentDate(@Param("date") LocalDateTime date);

  List<Appointment> findByClientIdOrderByAppointmentDateTimeDesc(Long clientId);

  List<Appointment> findByPetIdOrderByAppointmentDateTimeDesc(Long petId);

  List<Appointment> findByAssignedEmployeeIdAndAppointmentDateTimeBetween(
      Long employeeId, LocalDateTime start, LocalDateTime end);

  Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);

  List<Appointment> findByServiceType(ServiceType serviceType);

  @Query(
      value = "SELECT * FROM appointments WHERE DATE(appointment_date) = CURDATE()",
      nativeQuery = true)
  List<Appointment> findTodayAppointments();

  @Query(
      "SELECT a FROM Appointment a WHERE a.appointmentDateTime BETWEEN :now AND :tomorrow AND"
          + " a.status NOT IN ('CANCELADA', 'COMPLETADA') ORDER BY a.appointmentDateTime")
  List<Appointment> findUpcomingAppointments(
      @Param("now") LocalDateTime now, @Param("tomorrow") LocalDateTime tomorrow);

  @Query(
      "SELECT a FROM Appointment a WHERE a.assignedEmployee.id = :employeeId "
          + "AND a.status NOT IN ('CANCELADA') "
          + "AND a.appointmentDateTime < :endTime "
          + "AND a.appointmentDateTime >= :searchStart")
  List<Appointment> findConflictingAppointments(
      @Param("employeeId") Long employeeId,
      @Param("searchStart") LocalDateTime searchStart,
      @Param("endTime") LocalDateTime endTime);

  @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = :status")
  Long countByStatus(@Param("status") AppointmentStatus status);

  @Query("SELECT a.serviceType, COUNT(a) FROM Appointment a GROUP BY a.serviceType")
  List<Object[]> getAppointmentCountByServiceType();
}

