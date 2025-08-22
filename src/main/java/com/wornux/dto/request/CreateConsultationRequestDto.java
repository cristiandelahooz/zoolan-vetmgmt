package com.wornux.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConsultationRequestDto {
  @NotBlank(message = "Notes are required")
  private String notes;

  private String diagnosis;
  private String treatment;
  private String prescription;

  @NotNull(message = "Consultation date is required")
  private LocalDateTime consultationDate;

  @NotNull(message = "Pet ID is required")
  private Long petId;

  @NotNull(message = "Veterinarian ID is required")
  private Long veterinarianId;
}
