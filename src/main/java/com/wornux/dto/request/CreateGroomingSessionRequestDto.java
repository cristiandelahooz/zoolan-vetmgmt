package com.wornux.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor @AllArgsConstructor
public class CreateGroomingSessionRequestDto {

    @NotNull(message = "La mascota es obligatoria")
    private Long petId;


    private Long groomerId;

    @NotBlank(message = "Las notas son obligatorias")
    private String notes;


    private LocalDateTime groomingDate;
}
