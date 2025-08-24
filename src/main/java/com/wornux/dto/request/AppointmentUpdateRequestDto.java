package com.wornux.dto.request;

import static com.wornux.constants.AppointmentConstants.MAX_APPOINTMENT_NOTES_LENGTH;
import static com.wornux.constants.AppointmentConstants.MAX_REASON_LENGTH;

import com.vaadin.hilla.BrowserCallable;
import com.wornux.data.entity.AppointmentClientInfo;
import com.wornux.data.enums.AppointmentStatus;
import com.wornux.data.enums.OfferingType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Data
@BrowserCallable
public class AppointmentUpdateRequestDto {

  @Nullable
  @Future(message = "La fecha de inicio de la cita debe ser en el futuro")
  private LocalDateTime startAppointmentDate;

  @Nullable
  @Future(message = "La fecha de cierre de la cita debe ser en el futuro")
  private LocalDateTime endAppointmentDate;

  @Nullable private OfferingType offeringType;

  @Nullable private AppointmentStatus status;

  @Nullable
  @Size(max = MAX_REASON_LENGTH, message = "El motivo no puede exceder {max} caracteres")
  private String reason;

  @Size(
      max = MAX_APPOINTMENT_NOTES_LENGTH,
      message = "Las notas no pueden exceder {max} caracteres")
  @Nullable
  private String notes;

  @Nullable private Long clientId;

  @Nullable private Long petId;

  @Nullable private Long assignedEmployeeId;

  @Valid @Nullable private AppointmentClientInfo guestClientInfo;

  @Nullable private String createdBy;
}
