package com.wornux.validation.petsbreed;

import com.wornux.data.enums.PetType;

/**
 * Interface for objects that can be validated for pet breed compatibility. Any class that has both a PetType and a
 * breed String can implement this interface to be validated using the @ValidPetBreed annotation.
 */
public interface PetBreedValidatable {

    /**
     * Gets the pet type for validation.
     * 
     * @return the pet type, or null if not set
     */
    PetType getType();

    /**
     * Gets the breed for validation.
     * 
     * @return the breed string, or null if not set
     */
    String getBreed();
}
