package com.wornux.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.wornux.data.entity.AppointmentClientInfo;
import com.wornux.data.enums.AppointmentStatus;
import com.wornux.data.enums.ServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.wornux.constants.AppointmentConstants.*;
import static com.wornux.constants.ValidationConstants.DATE_PATTERN;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentCreateRequestDto {

    @NotNull(message = "La fecha y hora de inicio es obligatoria")
    @Future(message = "La fecha de inicio de la cita debe ser en el futuro")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    private LocalDateTime startAppointmentDate;

    @NotNull(message = "La fecha y hora de cierre es obligatoria")
    @Future(message = "La fecha de cierre de la cita debe ser en el futuro")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_PATTERN)
    private LocalDateTime endAppointmentDate;

    @NotNull(message = "El tipo de servicio es obligatorio")
    private ServiceType serviceType;

    private AppointmentStatus status;

    @Size(max = MAX_REASON_LENGTH, message = "El motivo no puede exceder {max} caracteres")
    @Nullable
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

    public String getAppointmentDateTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return startAppointmentDate.format(formatter) + " - " + endAppointmentDate.format(formatter);
    }
}