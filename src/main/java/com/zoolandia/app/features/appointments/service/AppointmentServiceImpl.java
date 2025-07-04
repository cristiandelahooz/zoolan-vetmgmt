package com.zoolandia.app.features.appointments.service;

import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.hilla.BrowserCallable;
import com.zoolandia.app.features.appointments.domain.Appointment;
import com.zoolandia.app.features.appointments.domain.AppointmentStatus;
import com.zoolandia.app.features.appointments.dtos.AppointmentCreateDTO;
import com.zoolandia.app.features.appointments.dtos.AppointmentResponseDTO;
import com.zoolandia.app.features.appointments.dtos.AppointmentUpdateDTO;
import com.zoolandia.app.features.appointments.mapper.AppointmentMapper;
import com.zoolandia.app.features.appointments.repository.AppointmentRepository;
import com.zoolandia.app.features.appointments.exceptions.AppointmentNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@BrowserCallable
@AnonymousAllowed
@Transactional
@Slf4j
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public AppointmentResponseDTO createAppointment(AppointmentCreateDTO createDTO) {
        log.debug("Creating appointment for {}", createDTO.getAppointmentDateTime());

        Appointment appointment = appointmentMapper.toEntity(createDTO);
        appointment = appointmentRepository.save(appointment);

        log.info("Created appointment with ID: {}", appointment.getId());
        return appointmentMapper.toResponseDTO(appointment);
    }

    @Override
    @Transactional
    public AppointmentResponseDTO updateAppointment(Long id, AppointmentUpdateDTO updateDTO) {
        log.debug("Updating appointment with ID: {}", id);

        Appointment appointment = findAppointmentById(id);

        appointmentMapper.updateAppointmentFromDTO(updateDTO, appointment);
        appointment = appointmentRepository.save(appointment);

        log.info("Updated appointment with ID: {}", id);
        return appointmentMapper.toResponseDTO(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDTO getAppointmentById(Long id) {
        Appointment appointment = findAppointmentById(id);
        return appointmentMapper.toResponseDTO(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream().map(appointmentMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDTO> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable).map(appointmentMapper::toResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Appointment> appointments = appointmentRepository.findByStartAppointmentDateBetween(start, end);
        return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByDate(LocalDateTime date) {
        List<Appointment> appointments = appointmentRepository.findByAppointmentDate(date);
        return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByClient(Long clientId) {
        List<Appointment> appointments = appointmentRepository.findByClientIdOrderByStartAppointmentDateDesc(clientId);
        return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByPet(Long petId) {
        List<Appointment> appointments = appointmentRepository.findByPetIdOrderByStartAppointmentDateDesc(petId);
        return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getAppointmentsByEmployee(Long employeeId, LocalDateTime start,
            LocalDateTime end) {
        List<Appointment> appointments = appointmentRepository.findByAssignedEmployeeIdAndStartAppointmentDateBetween(
                employeeId, start, end);
        return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getTodayAppointments() {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<Appointment> appointments = appointmentRepository.findTodayAppointments(startOfDay, endOfDay);
        return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getUpcomingAppointments() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);

        List<Appointment> appointments = appointmentRepository.findUpcomingAppointments(now, tomorrow);
        return appointments.stream().map(appointmentMapper::toResponseDTO).toList();
    }

    @Override
    @Transactional
    public AppointmentResponseDTO changeAppointmentStatus(Long id, AppointmentStatus newStatus) {
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

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getCalendarEvents(LocalDateTime start, LocalDateTime end) {
        return getAppointmentsByDateRange(start, end);
    }

    private Appointment findAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with ID: " + id));
    }
}