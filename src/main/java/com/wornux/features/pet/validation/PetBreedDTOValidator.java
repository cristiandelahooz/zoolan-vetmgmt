package com.wornux.features.pet.validation;

import com.wornux.features.pet.service.dto.PetCreateDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PetBreedDTOValidator implements ConstraintValidator<ValidPetBreedDTO, PetCreateDTO> {

  @Override
  public boolean isValid(PetCreateDTO dto, ConstraintValidatorContext context) {
    if (dto == null || dto.getType() == null || dto.getBreed() == null) {
      return true;
    }

    boolean isValid = dto.getType().getBreeds().contains(dto.getBreed());

    if (!isValid) {
      context.disableDefaultConstraintViolation();
      context
          .buildConstraintViolationWithTemplate(
              String.format(
                  "Breed '%s' is not valid for pet type '%s'",
                  dto.getBreed(), dto.getType().name()))
          .addPropertyNode("breed")
          .addConstraintViolation();
    }

    return isValid;
  }
}
