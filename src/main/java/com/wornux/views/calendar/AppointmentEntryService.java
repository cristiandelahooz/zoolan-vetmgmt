package com.wornux.views.calendar;

import com.wornux.data.enums.OfferingType;
import com.wornux.dto.response.AppointmentResponseDto;
import com.wornux.services.interfaces.AppointmentService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.vaadin.stefan.fullcalendar.Entry;

@Service
@RequiredArgsConstructor
public class AppointmentEntryService {

  private final AppointmentService appointmentService;

  public List<Entry> getAppointmentEntries() {
    return getAppointmentEntries(0, 1000);
  }

  public List<Entry> getAppointmentEntries(int page, int size) {
    List<AppointmentResponseDto> appointments =
        appointmentService.getAllAppointments(PageRequest.of(page, size)).getContent();
    return appointments.stream().map(this::convertToEntry).toList();
  }

  public List<Entry> getAppointmentEntriesInRange(LocalDateTime start, LocalDateTime end) {
    List<AppointmentResponseDto> appointments =
        appointmentService.getAllAppointments(PageRequest.of(0, 1000)).getContent();

    return appointments.stream()
        .filter(apt -> isWithinRange(apt, start, end))
        .map(this::convertToEntry)
        .toList();
  }

  private boolean isWithinRange(
      AppointmentResponseDto appointment, LocalDateTime start, LocalDateTime end) {
    LocalDateTime aptStart = appointment.getStartAppointmentDate();
    LocalDateTime aptEnd = appointment.getEndAppointmentDate();

    return aptStart != null
        && aptEnd != null
        && (aptStart.isBefore(end) || aptStart.isEqual(end))
        && (aptEnd.isAfter(start) || aptEnd.isEqual(start));
  }

  private Entry convertToEntry(AppointmentResponseDto appointment) {
    Entry entry = new Entry(String.valueOf(appointment.getEventId()));
    entry.setTitle(buildAppointmentTitle(appointment));
    entry.setStart(appointment.getStartAppointmentDate());
    entry.setEnd(appointment.getEndAppointmentDate());
    entry.setDescription(buildAppointmentDescription(appointment));

    AppointmentStyle style = getAppointmentStyle(appointment);
    entry.setColor(style.backgroundColor());
    entry.setBorderColor(style.borderColor());
    entry.setTextColor(style.textColor());
    entry.setEditable(true);

    return entry;
  }

  private String buildAppointmentTitle(AppointmentResponseDto appointment) {
    StringBuilder title = new StringBuilder();

    if (appointment.getPetName() != null) {
      title.append(appointment.getPetName());
    }

    if (appointment.getOfferingType() != null) {
      if (!title.isEmpty()) {
        title.append(" - ");
      }
      title.append(getOfferingTypeDisplayName(appointment.getOfferingType()));
    }

    if (title.isEmpty()) {
      title.append(
          appointment.getAppointmentTitle() != null ? appointment.getAppointmentTitle() : "Cita");
    }

    return title.toString();
  }

  private String buildAppointmentDescription(AppointmentResponseDto appointment) {
    StringBuilder desc = new StringBuilder();

    if (appointment.getPetName() != null) {
      desc.append("Mascota: ").append(appointment.getPetName()).append("\n");
    }

    if (appointment.getOfferingType() != null) {
      desc.append("Servicio: ")
          .append(getOfferingTypeDisplayName(appointment.getOfferingType()))
          .append("\n");
    }

    if (appointment.getAppointmentTitle() != null) {
      desc.append("Título: ").append(appointment.getAppointmentTitle());
    }

    return desc.toString().trim();
  }

  private String getOfferingTypeDisplayName(OfferingType offeringType) {
    return switch (offeringType) {
      case CONSULTATION -> "Consulta";
      case VACCINATION -> "Vacunación";
      case GROOMING -> "Peluquería";
      default -> offeringType.name();
    };
  }

  private AppointmentStyle getAppointmentStyle(AppointmentResponseDto appointment) {
    if (appointment.getOfferingType() != null) {
      return switch (appointment.getOfferingType()) {
        case CONSULTATION -> new AppointmentStyle("#E3F2FD", "#2196F3", "#1565C0");
        case VACCINATION -> new AppointmentStyle("#E8F5E8", "#4CAF50", "#2E7D32");
        case GROOMING -> new AppointmentStyle("#F3E5F5", "#9C27B0", "#6A1B9A");
        default -> new AppointmentStyle("#F5F5F5", "#607D8B", "#37474F");
      };
    }
    return new AppointmentStyle("#F5F5F5", "#607D8B", "#37474F");
  }

  public Optional<AppointmentResponseDto> getAppointment(Long appointmentId) {
    try {
      return Optional.ofNullable(appointmentService.getAppointmentById(appointmentId));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private record AppointmentStyle(String backgroundColor, String borderColor, String textColor) {}
}
