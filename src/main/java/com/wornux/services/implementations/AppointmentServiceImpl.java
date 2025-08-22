package com.wornux.services.implementations;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.Appointment;
import com.wornux.data.enums.AppointmentStatus;
import com.wornux.data.repository.AppointmentRepository;
import com.wornux.dto.request.AppointmentCreateRequestDto;
import com.wornux.dto.request.AppointmentUpdateRequestDto;
import com.wornux.dto.response.AppointmentResponseDto;
import com.wornux.exception.AppointmentNotFoundException;
import com.wornux.mapper.AppointmentMapper;
import com.wornux.services.interfaces.AppointmentService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@BrowserCallable
@Transactional
@Slf4j
// TODO: remove this annotation to restrict access to authenticated users only
@AnonymousAllowed
public class AppointmentServiceImpl implements AppointmentService {

  private final AppointmentRepository appointmentRepository;
  private final AppointmentMapper appointmentMapper;

  @Override
  @Transactional
  public AppointmentResponseDto createAppointment(AppointmentCreateRequestDto createRequest) {
    log.debug("Creating appointment for {}", createRequest.getAppointmentDateTime());

    Appointment appointment = appointmentMapper.toEntity(createRequest);
    appointment = appointmentRepository.save(appointment);

    log.info("Created appointment with ID: {}", appointment.getId());
    return appointmentMapper.toResponseDTO(appointment);
  }

  @Override
  @Transactional
  public AppointmentResponseDto updateAppointment(
      Long id, AppointmentUpdateRequestDto updateRequest) {
    log.debug("Updating appointment with ID: {}", id);

    Appointment appointment = findAppointmentById(id);

    appointmentMapper.updateAppointmentFromDTO(updateRequest, appointment);
    appointment = appointmentRepository.save(appointment);

    log.info("Updated appointment with ID: {}", id);
    return appointmentMapper.toResponseDTO(appointment);
  }

  @Override
  @Transactional(readOnly = true)
  public AppointmentResponseDto getAppointmentById(Long id) {
    Appointment appointment = findAppointmentById(id);
    return appointmentMapper.toResponseDTO(appointment);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AppointmentResponseDto> getAllAppointments() {
    return appointmentRepository.findAll().stream().map(appointmentMapper::toResponseDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AppointmentResponseDto> getAllAppointments(Pageable pageable) {
    return appointmentRepository.findAll(pageable).map(appointmentMapper::toResponseDTO);
  }

  @Override
  @Transactional(readOnly = true)
  public List<AppointmentResponseDto> getAppointmentsByDateRange(
      LocalDateTime start, LocalDateTime end) {
    List<Appointment> appointments =
        appointmentRepository.findByStartAppointmentDateBetween(start, end);
    return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AppointmentResponseDto> getAppointmentsByDate(LocalDateTime date) {
    List<Appointment> appointments = appointmentRepository.findByAppointmentDate(date);
    return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AppointmentResponseDto> getAppointmentsByClient(Long clientId) {
    List<Appointment> appointments =
        appointmentRepository.findByClientIdOrderByStartAppointmentDateDesc(clientId);
    return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AppointmentResponseDto> getAppointmentsByPet(Long petId) {
    List<Appointment> appointments =
        appointmentRepository.findByPetIdOrderByStartAppointmentDateDesc(petId);
    return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AppointmentResponseDto> getAppointmentsByEmployee(
      Long employeeId, LocalDateTime start, LocalDateTime end) {
    List<Appointment> appointments =
        appointmentRepository.findByAssignedEmployeeIdAndStartAppointmentDateBetween(
            employeeId, start, end);
    return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AppointmentResponseDto> getTodayAppointments() {
    LocalDateTime today = LocalDateTime.now();
    LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
    LocalDateTime endOfDay = startOfDay.plusDays(1);
    List<Appointment> appointments =
        appointmentRepository.findTodayAppointments(startOfDay, endOfDay);
    return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AppointmentResponseDto> getUpcomingAppointments() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime tomorrow = now.plusDays(1);

    List<Appointment> appointments = appointmentRepository.findUpcomingAppointments(now, tomorrow);
    return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
  }

  @Override
  @Transactional
  public AppointmentResponseDto changeAppointmentStatus(Long id, AppointmentStatus newStatus) {
    log.debug("Changing status of appointment {} to {}", id, newStatus);

    Appointment appointment = findAppointmentById(id);
    appointment.setStatus(newStatus);
    appointment = appointmentRepository.save(appointment);

    log.info("Changed status of appointment {} to {}", id, newStatus);
    return appointmentMapper.toResponseDTO(appointment);
  }

  @Override
  @Transactional
  public void cancelAppointment(Long id, String reason) {
    log.debug("Cancelling appointment with ID: {}", id);

    Appointment appointment = findAppointmentById(id);
    appointment.setStatus(AppointmentStatus.CANCELADA);
    if (reason != null && !reason.trim().isEmpty()) {
      String currentNotes = appointment.getNotes() != null ? appointment.getNotes() : "";
      appointment.setNotes(currentNotes + "\nMotivo de cancelación: " + reason);
    }
    appointmentRepository.save(appointment);

    log.info("Cancelled appointment with ID: {}", id);
  }

  @Override
  @Transactional
  public void deleteAppointment(Long id) {
    log.debug("Deleting appointment with ID: {}", id);

    if (!appointmentRepository.existsById(id)) {
      throw new AppointmentNotFoundException("Appointment not found with ID: " + id);
    }

    appointmentRepository.deleteById(id);
    log.info("Deleted appointment with ID: {}", id);
  }

  private Appointment findAppointmentById(Long id) {
    return appointmentRepository
        .findById(id)
        .orElseThrow(
            () -> new AppointmentNotFoundException("Appointment not found with ID: " + id));
  }
}
