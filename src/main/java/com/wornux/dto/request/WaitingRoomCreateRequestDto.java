package com.wornux.dto.request;

import com.wornux.data.enums.Priority;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;
import com.wornux.data.enums.VisitType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitingRoomCreateRequestDto {

  @NotNull(message = "El cliente es requerido")
  private Long clientId;

  @NotNull(message = "La mascota es requerida")
  private Long petId;

  @NotBlank(message = "La razón de la visita es requerida")
  private String reasonForVisit;

  @NotNull(message = "El tipo es requerido")
  @Builder.Default
  private VisitType type = VisitType.MEDICA;

  @NotNull(message = "La prioridad es requerida")
  private Priority priority;

  private String notes;

  @NotNull(message = "La hora de llegada es requerida")
  @Future(message = "La hora de llegada debe ser en el futuro")
  private LocalDateTime arrivalTime;
}
