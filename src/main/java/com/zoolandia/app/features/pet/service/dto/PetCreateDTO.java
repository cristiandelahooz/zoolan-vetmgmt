package com.zoolandia.app.features.pet.service.dto;

import com.zoolandia.app.features.pet.domain.PetBreed;
import com.zoolandia.app.features.pet.domain.PetType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PetCreateDTO {

    @NotBlank(message = "El nombre de la mascota es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @NotNull(message = "El tipo de mascota es requerido")
    private PetType type;

    @NotNull(message = "La raza es requerida")
    private PetBreed breed;

    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    private LocalDate birthDate;

    @NotNull(message = "Debe asociarse a un cliente")
    private Long ownerId;
}
