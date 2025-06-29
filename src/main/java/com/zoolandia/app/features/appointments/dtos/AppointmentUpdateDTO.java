package com.zoolandia.app.features.appointments.dtos;

import com.vaadin.hilla.BrowserCallable;
import com.zoolandia.app.features.appointments.domain.AppointmentStatus;
import com.zoolandia.app.features.appointments.domain.ServiceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

import static com.zoolandia.app.common.constants.AppointmentConstants.*;

@Data
@BrowserCallable
public class AppointmentUpdateDTO {
    
    @Future(message = "La fecha de la cita debe ser en el futuro")
    @Nullable
    private LocalDateTime appointmentDateTime;
    
    @Min(value = MIN_APPOINTMENT_DURATION_MINUTES, message = "La duración mínima es {value} minutos")
    @Max(value = MAX_APPOINTMENT_DURATION_MINUTES, message = "La duración máxima es {value} minutos")
    @Nullable
    private Integer durationMinutes;
    
    @Nullable
    private ServiceType serviceType;
    
    @Nullable
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
    private AppointmentClientInfoDTO guestClientInfo;
    
    @Nullable
    private String updatedBy;
}