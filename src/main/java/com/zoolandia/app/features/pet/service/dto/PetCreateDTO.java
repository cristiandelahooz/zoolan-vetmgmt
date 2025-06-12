package com.zoolandia.app.features.pet.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zoolandia.app.features.pet.domain.Gender;
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

    private LocalDate birthDate;

    @AssertTrue(message = "La fecha no puede ser futura")
    @JsonIgnore
    public boolean isBirthDateValid() {
        return birthDate == null || !birthDate.isAfter(LocalDate.now());
    }

    @NotNull(message = "Debe asociarse a un cliente")
    private Long ownerId;

    @NotNull(message = "El género es requerido")
    private Gender gender;
}
