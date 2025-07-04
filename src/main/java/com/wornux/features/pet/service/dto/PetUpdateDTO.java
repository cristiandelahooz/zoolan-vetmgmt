package com.wornux.features.pet.service.dto;

import com.wornux.dto.Gender;
import com.wornux.features.pet.domain.PetType;
import com.wornux.features.pet.validation.ValidPetBreedDTO;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@ValidPetBreedDTO
public class PetUpdateDTO {

    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    private PetType type;

    private String breed;

    private LocalDate birthDate;

    @AssertTrue(message = "La fecha no puede ser futura")
    public boolean isBirthDateValid() {
        return birthDate == null || !birthDate.isAfter(LocalDate.now());
    }

    private Long ownerId;

    @NotNull(message = "El género es requerido")
    private Gender gender;
}
