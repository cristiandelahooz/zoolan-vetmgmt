package com.wornux.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGroomingSessionRequestDto {
  private Long petId;
  private Long groomerId;

  @NotBlank(message = "Las notas no pueden estar vacías")
  private String notes;

  private LocalDateTime groomingDate;
  private Boolean active;
}
