package com.zoolandia.app.features.consultation.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateConsultationDTO {
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
