package com.zoolandia.app.features.appointments.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.zoolandia.app.features.appointments.domain.AppointmentClientInfo;
import com.zoolandia.app.features.appointments.domain.AppointmentStatus;
import com.zoolandia.app.features.appointments.domain.ServiceType;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

import static com.zoolandia.app.common.constants.ValidationConstants.DATE_PATTERN;

@Data
public class AppointmentResponseDTO {

    private Long eventId;

    private String appointmentTitle;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    private LocalDateTime startAppointmentDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    private LocalDateTime endAppointmentDate;

    private ServiceType serviceType;

    private AppointmentStatus status;

    @Nullable
    private String reason;

    @Nullable
    private String notes;

    @Nullable
    private String clientName;

    @Nullable
    private String clientContactPhone;

    @Nullable
    private String petName;

    @Nullable
    private String assignedEmployeeName;

    @Nullable
    private AppointmentClientInfo guestClientInfo;

    @Nullable
    private String createdBy;

    @Nullable
    private String updatedBy;

    private boolean completed;

    private boolean cancelled;

    private boolean hasRegisteredClient;

    private boolean requiresVeterinarian;
}