package com.wornux.services.interfaces;

import com.wornux.data.enums.AppointmentStatus;
import com.wornux.dto.request.AppointmentCreateRequestDto;
import com.wornux.dto.request.AppointmentUpdateRequestDto;
import com.wornux.dto.response.AppointmentResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

  List<AppointmentResponseDto> getAppointmentsByEmployee(
      Long employeeId, LocalDateTime start, LocalDateTime end);

  List<AppointmentResponseDto> getTodayAppointments();

  List<AppointmentResponseDto> getUpcomingAppointments();

  AppointmentResponseDto changeAppointmentStatus(Long id, AppointmentStatus newStatus);

  void cancelAppointment(Long id, String reason);

  void deleteAppointment(Long id);
}
