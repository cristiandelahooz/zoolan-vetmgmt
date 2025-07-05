package com.wornux.service.interfaces;

import com.wornux.data.enums.AppointmentStatus;
import com.wornux.dto.request.AppointmentCreateRequestDto;
import com.wornux.dto.response.AppointmentResponseDto;
import com.wornux.dto.request.AppointmentUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentService {

    AppointmentResponseDto createAppointment(AppointmentCreateRequestDto createDTO);

    AppointmentResponseDto updateAppointment(Long id, AppointmentUpdateRequestDto updateDTO);

    AppointmentResponseDto getAppointmentById(Long id);

    Page<AppointmentResponseDto> getAllAppointments(Pageable pageable);

    List<AppointmentResponseDto> getAllAppointments();

    List<AppointmentResponseDto> getAppointmentsByDateRange(LocalDateTime start, LocalDateTime end);

    List<AppointmentResponseDto> getAppointmentsByDate(LocalDateTime date);

    List<AppointmentResponseDto> getAppointmentsByClient(Long clientId);

    List<AppointmentResponseDto> getAppointmentsByPet(Long petId);

    List<AppointmentResponseDto> getAppointmentsByEmployee(Long employeeId, LocalDateTime start, LocalDateTime end);

    List<AppointmentResponseDto> getTodayAppointments();

    List<AppointmentResponseDto> getUpcomingAppointments();

    AppointmentResponseDto changeAppointmentStatus(Long id, AppointmentStatus newStatus);

    void cancelAppointment(Long id, String reason);

    void deleteAppointment(Long id);

    // Para FullCalendar
    List<AppointmentResponseDto> getCalendarEvents(LocalDateTime start, LocalDateTime end);
}