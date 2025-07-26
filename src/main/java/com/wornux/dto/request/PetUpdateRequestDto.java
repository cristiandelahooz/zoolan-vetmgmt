package com.wornux.dto.request;

import com.wornux.data.enums.Gender;
import com.wornux.data.enums.PetType;
import com.wornux.validation.petsbreed.PetBreedValidatable;
import com.wornux.validation.petsbreed.ValidPetBreed;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Data;
import com.wornux.data.enums.FurType;
import com.wornux.data.enums.PetSize;



@Data
@ValidPetBreed
public class PetUpdateRequestDto implements PetBreedValidatable {

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

    @AssertTrue(message = "La raza no es válida para el tipo de mascota seleccionado")
    public boolean isValidBreed() {
        if (type == null || breed == null) {
            return true;
        }
        return type.isValidBreedForType(breed);
    }

    @Size(max = 50, message = "El color no puede exceder 50 caracteres")
    private String color;

    private PetSize size;

    private FurType furType;

}