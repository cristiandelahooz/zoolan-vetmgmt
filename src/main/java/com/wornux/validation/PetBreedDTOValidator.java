package com.wornux.validation;

import com.wornux.dto.request.PetCreateRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PetBreedDTOValidator implements ConstraintValidator<ValidPetBreedDTO, PetCreateRequestDto> {

    @Override
    public boolean isValid(PetCreateRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.getType() == null || dto.getBreed() == null) {
            return true;
        }

        boolean isValid = dto.getType().getBreeds().contains(dto.getBreed());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Breed '%s' is not valid for pet type '%s'", dto.getBreed(), dto.getType().name()))
                    .addPropertyNode("breed").addConstraintViolation();
        }

        return isValid;
    }
}
