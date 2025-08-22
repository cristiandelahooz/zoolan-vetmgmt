package com.wornux.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wornux.data.enums.FurType;
import com.wornux.data.enums.Gender;
import com.wornux.data.enums.PetSize;
import com.wornux.data.enums.PetType;
import com.wornux.validation.petsbreed.PetBreedValidatable;
import com.wornux.validation.petsbreed.ValidPetBreed;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ValidPetBreed
public class PetCreateRequestDto implements PetBreedValidatable {

    @NotBlank(message = "El nombre de la mascota es requerido")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String name;

    @NotNull(message = "El tipo de mascota es requerido")
    private PetType type;

    @NotNull(message = "La raza es requerida")
    private String breed;

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

    @Size(max = 50, message = "El color no puede exceder 50 caracteres")
    private String color;

    @NotNull(message = "El tamaño es requerido")
    private PetSize size;

    @NotNull(message = "El tipo de pelo es requerido")
    private FurType furType;
}
