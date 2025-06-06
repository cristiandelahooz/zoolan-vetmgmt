package com.zoolandia.app.features.pet.service.dto;

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

    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    private LocalDate birthDate;

    private Long ownerId;
}

