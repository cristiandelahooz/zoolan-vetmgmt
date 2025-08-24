package com.wornux.dto.response;

import com.wornux.data.entity.AppointmentClientInfo;
import com.wornux.data.enums.AppointmentStatus;
import com.wornux.data.enums.OfferingType;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@Data
public class AppointmentResponseDto {

  private Long eventId;

  private String appointmentTitle;

  private LocalDateTime startAppointmentDate;

  private LocalDateTime endAppointmentDate;

  private OfferingType offeringType;

  private AppointmentStatus status;

  @Nullable private String reason;

  @Nullable private String notes;

  @Nullable private String clientName;

  @Nullable private String clientContactPhone;

  @Nullable private String petName;

  @Nullable private String petBreed;

  @Nullable private String assignedEmployeeName;

  @Nullable private AppointmentClientInfo guestClientInfo;

  @Nullable private String createdBy;

  @Nullable private String updatedBy;

  private boolean completed;

  private boolean cancelled;

  private boolean hasRegisteredClient;

  private boolean requiresVeterinarian;
}
