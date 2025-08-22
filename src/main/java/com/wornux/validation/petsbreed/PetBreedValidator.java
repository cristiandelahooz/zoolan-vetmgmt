package com.wornux.validation.petsbreed;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Generic validator for pet breed compatibility. Works with any class that implements
 * PetBreedValidatable.
 */
public class PetBreedValidator implements ConstraintValidator<ValidPetBreed, PetBreedValidatable> {

    @Override
    public boolean isValid(PetBreedValidatable validatable, ConstraintValidatorContext context) {
        if (validatable == null) {
            return true;
        }

        return PetBreedValidationHelper.isValidBreedForType(validatable.getType(), validatable.getBreed(), context);
    }
}
