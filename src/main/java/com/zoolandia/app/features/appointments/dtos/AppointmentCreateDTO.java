package com.zoolandia.app.features.appointments.dtos;

import com.zoolandia.app.features.appointments.domain.ServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

import static com.zoolandia.app.common.constants.AppointmentConstants.*;

@Data
public class AppointmentCreateDTO {
    
    @NotNull(message = "La fecha y hora de la cita es obligatoria")
    @Future(message = "La fecha de la cita debe ser en el futuro")
    private LocalDateTime appointmentDateTime;
    
    @Min(value = MIN_APPOINTMENT_DURATION_MINUTES, message = "La duración mínima es {value} minutos")
    @Max(value = MAX_APPOINTMENT_DURATION_MINUTES, message = "La duración máxima es {value} minutos")
    private Integer durationMinutes = DEFAULT_APPOINTMENT_DURATION_MINUTES;
    
    @NotNull(message = "El tipo de servicio es obligatorio")
    private ServiceType serviceType;
    
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
    private AppointmentClientInfoDTO guestClientInfo;
    
    @Nullable
    private String createdBy;
}