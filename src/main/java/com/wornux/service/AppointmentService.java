package com.wornux.service;

import com.wornux.domain.AppointmentStatus;
import com.wornux.dto.AppointmentCreateDTO;
import com.wornux.dto.AppointmentResponseDTO;
import com.wornux.dto.AppointmentUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {

    AppointmentResponseDTO createAppointment(AppointmentCreateDTO createDTO);

    AppointmentResponseDTO updateAppointment(Long id, AppointmentUpdateDTO updateDTO);

    AppointmentResponseDTO getAppointmentById(Long id);

    Page<AppointmentResponseDTO> getAllAppointments(Pageable pageable);

    List<AppointmentResponseDTO> getAllAppointments();

    List<AppointmentResponseDTO> getAppointmentsByDateRange(LocalDateTime start, LocalDateTime end);

    List<AppointmentResponseDTO> getAppointmentsByDate(LocalDateTime date);

    List<AppointmentResponseDTO> getAppointmentsByClient(Long clientId);

    List<AppointmentResponseDTO> getAppointmentsByPet(Long petId);

    List<AppointmentResponseDTO> getAppointmentsByEmployee(Long employeeId, LocalDateTime start, LocalDateTime end);

    List<AppointmentResponseDTO> getTodayAppointments();

    List<AppointmentResponseDTO> getUpcomingAppointments();

    AppointmentResponseDTO changeAppointmentStatus(Long id, AppointmentStatus newStatus);

    void cancelAppointment(Long id, String reason);

    void deleteAppointment(Long id);

    // Para FullCalendar
    List<AppointmentResponseDTO> getCalendarEvents(LocalDateTime start, LocalDateTime end);
}