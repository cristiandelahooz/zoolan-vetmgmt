package com.zoolandia.app.features.pet.service.dto;

import com.zoolandia.app.features.pet.domain.Gender;
import com.zoolandia.app.features.pet.domain.PetBreed;
import com.zoolandia.app.features.pet.domain.PetType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PetUpdateDTO {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    private PetType type;

    private PetBreed breed;

    private LocalDate birthDate;

    @AssertTrue(message = "La fecha no puede ser futura")
    public boolean isBirthDateValid() {
        return birthDate == null || !birthDate.isAfter(LocalDate.now());
    }

    private Long ownerId;

    @NotNull(message = "El género es requerido")
    private Gender gender;
}

