package com.wornux.dto;

import com.wornux.dto.Gender;
import com.wornux.domain.PetType;
import com.wornux.validation.ValidPetBreedDTO;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Data;

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
