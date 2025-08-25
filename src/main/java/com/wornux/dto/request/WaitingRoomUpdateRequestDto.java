package com.wornux.dto.request;

import com.wornux.data.enums.Priority;
import com.wornux.data.enums.VisitType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitingRoomUpdateRequestDto {

  @NotNull(message = "El ID de la entrada es requerido")
  private Long id;

  @NotNull(message = "El cliente es requerido")
  private Long clientId;

  @NotNull(message = "La mascota es requerida")
  private Long petId;

  @NotBlank(message = "La razón de la visita es requerida")
  private String reasonForVisit;

  @NotNull(message = "La prioridad es requerida")
  private Priority priority;

  @NotNull(message = "El tipo es requerido")
  @Builder.Default
  private VisitType type = VisitType.MEDICA;

  private String notes;

  @NotNull(message = "La hora de llegada es requerida")
  @Future(message = "La hora de llegada debe ser en el futuro")
  private LocalDateTime arrivalTime;
}
