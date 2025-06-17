package com.zoolandia.app.features.pet.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PetBreedValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPetBreed {
    String message() default "The breed is not valid for the selected pet type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}