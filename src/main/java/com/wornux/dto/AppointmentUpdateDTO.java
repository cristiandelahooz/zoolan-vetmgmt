package com.wornux.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.vaadin.hilla.BrowserCallable;
import com.wornux.domain.AppointmentClientInfo;
import com.wornux.domain.AppointmentStatus;
import com.wornux.domain.ServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

import static com.wornux.constants.AppointmentConstants.*;
import static com.wornux.constants.ValidationConstants.DATE_PATTERN;

@Data
@BrowserCallable
public class AppointmentUpdateDTO {

    @Nullable
    @Future(message = "La fecha de inicio de la cita debe ser en el futuro")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    private LocalDateTime startAppointmentDate;

    @Nullable
    @Future(message = "La fecha de cierre de la cita debe ser en el futuro")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    private LocalDateTime endAppointmentDate;

    @Nullable
    private ServiceType serviceType;

    @Nullable
    private AppointmentStatus status;

    @Nullable
    @Size(max = MAX_REASON_LENGTH, message = "El motivo no puede exceder {max} caracteres")
    private String reason;

    @Size(max = MAX_APPOINTMENT_NOTES_LENGTH, message = "Las notas no pueden exceder {max} caracteres")
    @Nullable
    private String notes;

    @Nullable
    private Long clientId;

    @Nullable
    private Long petId;

    @Nullable
    private Long assignedEmployeeId;

    @Valid
    @Nullable
    private AppointmentClientInfo guestClientInfo;

    @Nullable
    private String createdBy;
}