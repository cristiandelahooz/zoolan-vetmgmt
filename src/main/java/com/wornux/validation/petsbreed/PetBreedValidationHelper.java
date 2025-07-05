package com.wornux.validation.petsbreed;

import com.wornux.data.enums.PetType;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Helper class for pet breed validation logic.
 * Centralizes the validation logic to avoid code duplication and circular dependencies.
 */
public final class PetBreedValidationHelper {
    
    private PetBreedValidationHelper() {
        // Utility class
    }
    
    /**
     * Validates if a breed is valid for the given pet type.
     * 
     * @param petType the pet type to validate against
     * @param breed the breed to validate
     * @param context the validation context for error reporting
     * @return true if valid, false otherwise
     */
    public static boolean isValidBreedForType(PetType petType, String breed, ConstraintValidatorContext context) {
        if (petType == null || breed == null) {
            return true;
        }
        
        boolean isValid = petType.isValidBreedForType(breed);
        
        if (!isValid && context != null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Breed '%s' is not valid for pet type '%s'", breed, petType.name()))
                    .addPropertyNode("breed").addConstraintViolation();
        }
        
        return isValid;
    }
    
    /**
     * Validates if a breed is valid for the given pet type without context.
     * 
     * @param petType the pet type to validate against
     * @param breed the breed to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValidBreedForType(PetType petType, String breed) {
        return isValidBreedForType(petType, breed, null);
    }
}
