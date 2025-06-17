package com.zoolandia.app.features.pet.validation;

import com.zoolandia.app.features.pet.domain.Pet;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PetBreedValidator implements ConstraintValidator<ValidPetBreed, Pet> {

    @Override
    public void initialize(ValidPetBreed constraintAnnotation) {
    }

    @Override
    public boolean isValid(Pet pet, ConstraintValidatorContext context) {
        if (pet == null || pet.getType() == null || pet.getBreed() == null) {
            return true;
        }

        boolean isValid = pet.getType().isValidBreedForType(pet.getBreed());

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                            String.format("Breed '%s' is not valid for pet type '%s'",
                                    pet.getBreed(), pet.getType().name()))
                    .addPropertyNode("breed")
                    .addConstraintViolation();
        }

        return isValid;
    }
}